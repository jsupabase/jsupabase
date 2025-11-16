package io.github.jsupabase.storage; // O el paquete de tu módulo de pruebas

import io.github.jsupabase.auth.dto.AuthResponse;
import io.github.jsupabase.client.SupabaseClient;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.realtime.RealtimeChannelBuilder;
import io.github.jsupabase.realtime.dto.options.PostgresChangesFilter;
import io.github.jsupabase.realtime.enums.RealtimeEvent;
import io.github.jsupabase.storage.dto.TestRecord;
import io.github.jsupabase.storage.dto.UploadResponse;
import io.github.jsupabase.storage.dto.options.CreateBucketOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * SuperMain para probar la integración de TODOS los módulos del SDK
 * (Auth, Postgrest, Realtime, Storage) usando la arquitectura Gateway.
 */
public class SuperAllModulesMain {

    // --- ¡DEBES REEMPLAZAR ESTO! ---
    private static final String SUPABASE_URL = "https://plddnnwzjpzequtfllpm.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBsZGRubnd6anB6ZXF1dGZsbHBtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyMDg3ODksImV4cCI6MjA3ODc4NDc4OX0.0qqKAP_k-GISK0Fd-F3LxcjxGrrwjalQE5-aFKP8z8c";
    // --- Fin de Reemplazos ---

    private static final String DEMO_EMAIL = "gacej77015@chaineor.com";
    private static final String DEMO_PASSWORD = "password1234";

    private static final String TEST_TABLE = "tabla";
    private static final String TEST_BUCKET = "jsupabase-full-test-" + ThreadLocalRandom.current().nextInt(1000);
    private static final Long TEST_ID = ThreadLocalRandom.current().nextLong(100000, 999999);

    private static SupabaseClient supabase;

    public static void main(String[] args) {
        System.out.println("--- Iniciando Prueba de Integración Completa del SDK ---");
        Path tempFile = null;

        try {
            // --- 1. CONFIGURACIÓN ---
            SupabaseConfig config = new SupabaseConfig.Builder(SUPABASE_URL, SUPABASE_ANON_KEY).build();
            supabase = SupabaseClient.create(config);

            // --- 2. AUTENTICACIÓN ---
            System.out.println("\n[Paso 1: Auth] Iniciando sesión...");
            AuthResponse authResponse = supabase.auth().signInWithPassword(DEMO_EMAIL, DEMO_PASSWORD).join();
            System.out.println("[Auth] Login exitoso. Usuario: " + authResponse.getUser().getEmail());
            System.out.println("[Auth] El 'SupabaseClient' ha reconfigurado Postgrest, Storage y Realtime internamente.");

            // --- 3. REALTIME (PREPARACIÓN) ---
            System.out.println("\n[Paso 2: Realtime] Configurando listener...");

            CountDownLatch realtimeLatch = new CountDownLatch(1);

            PostgresChangesFilter insertFilter = new PostgresChangesFilter.Builder(RealtimeEvent.INSERT)
                    .schema("public")
                    .table(TEST_TABLE)
                    .callback(payload -> {
                        System.out.println("\n--- ¡EVENTO REALTIME RECIBIDO! ---");
                        System.out.println(payload);
                        System.out.println("-----------------------------------\n");
                        realtimeLatch.countDown();
                    })
                    .build();

            String topic = "realtime:public:" + TEST_TABLE;
            RealtimeChannelBuilder channel = supabase.realtime().channel(topic)
                    .onPostgresChanges(insertFilter)
                    .subscribe(status -> {
                        System.out.println("[Realtime] Estado de suscripción: " + status);
                    });

            Thread.sleep(2000);

            // --- 4. POSTGREST (ACCIÓN) ---
            System.out.println("\n[Paso 3: Postgrest] Insertando fila para disparar el evento...");
            TestRecord newRecord = new TestRecord(TEST_ID, "Full-SDK-Test", "LIVE");

            String insertResult = supabase.postgrest()
                    .table(TEST_TABLE)
                    .insert(newRecord)
                    .execute().join();
            System.out.println("[Postgrest] Fila insertada: " + insertResult);

            // --- 5. REALTIME (VERIFICACIÓN) ---
            System.out.println("\n[Paso 4: Realtime] Esperando evento (max 10s)...");
            boolean eventReceived = realtimeLatch.await(10, TimeUnit.SECONDS);

            if (!eventReceived) {
                throw new RuntimeException("¡FALLO! El evento de Realtime (INSERT) no se recibió en 10 segundos.");
            }
            System.out.println("[Realtime] ¡Éxito! Evento INSERT recibido.");
            channel.unsubscribe(); // Limpieza del canal

            // --- 6. STORAGE (ACCIÓN) ---
            System.out.println("\n[Paso 5: Storage] Probando creación de bucket y subida...");

            CreateBucketOptions options = new CreateBucketOptions.Builder(TEST_BUCKET).setPublic(false).build();
            supabase.storage().bucket().createBucket(options).join();
            System.out.println("[Storage] Bucket '" + TEST_BUCKET + "' creado.");

            tempFile = Files.createTempFile("jsupabase_full_test", ".txt");
            String fileContent = "Test de integración completo de jsupabase: OK";
            Files.writeString(tempFile, fileContent, StandardCharsets.UTF_8);

            String storagePath = "test-suite/full-test.txt";
            UploadResponse uploadResponse = supabase.storage()
                    .object(TEST_BUCKET)
                    .upload(storagePath, tempFile, false)
                    .join();
            System.out.println("[Storage] Archivo subido exitosamente a: " + uploadResponse.getFullPath());

            System.out.println("\n--- ¡ÉXITO! TODOS LOS MÓDULOS (Auth, Postgrest, Realtime, Storage) FUNCIONAN ---");

        } catch (Exception e) {
            System.err.println("\n--- ¡ERROR CRÍTICO EN LA PRUEBA DE INTEGRACIÓN! ---");
            e.printStackTrace();
            System.exit(1);
        } finally {
            // --- 7. LIMPIEZA FINAL ---
            System.out.println("\n[Paso 6: Limpieza] Eliminando recursos...");
            try {
                if (tempFile != null) Files.deleteIfExists(tempFile);

                System.out.println("[Limpieza] Vaciando bucket '" + TEST_BUCKET + "'...");
                supabase.storage().bucket().emptyBucket(TEST_BUCKET).join();

                // --- ¡PAUSA AÑADIDA! ---
                System.out.println("[Limpieza] Esperando 10 segundos a que el servidor procese el vaciado...");
                Thread.sleep(10000); // 10 segundos de espera

                System.out.println("[Limpieza] Eliminando bucket...");
                supabase.storage().bucket().deleteBucket(TEST_BUCKET).join();

                System.out.println("[Limpieza] Eliminando fila de prueba de '" + TEST_TABLE + "'...");
                supabase.postgrest().table(TEST_TABLE).delete().eq("id", TEST_ID).execute().join();

                supabase.auth().signOut().join();
                System.out.println("[Limpieza] Sesión cerrada.");

            } catch (Exception e) {
                System.err.println("Error durante la limpieza: " + e.getMessage());
            }
            System.out.println("\nSuperMain finalizado.");
            System.exit(0);
        }
    }
}