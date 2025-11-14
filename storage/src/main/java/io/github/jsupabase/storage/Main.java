package io.github.jsupabase.storage;

// 1. IMPORTAR EL CLIENTE PRINCIPAL

import io.github.jsupabase.client.SupabaseClient;
import io.github.jsupabase.auth.dto.AuthResponse;
import io.github.jsupabase.core.config.SupabaseConfig;
// (Ya no necesitamos importar AuthClient o PostgrestClient directamente)

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Ejemplo de cómo usar el SupabaseClient principal para
 * crear un usuario e insertar datos de forma autenticada.
 */
public class Main {

    // --- ¡DEBES REEMPLAZAR ESTO! ---
    // Esta es la URL de tu API (no la de la base de datos)
    private static final String SUPABASE_URL = "https://ptymzcijjlzpiojgmpav.supabase.co"; // Ya la tenías
    // Esta es tu clave pública anónima (public anon key)
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB0eW16Y2lqamx6cGlvamdtcGF2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMxMTc1NzksImV4cCI6MjA3ODY5MzU3OX0.0dDOhEEsc6CX8EF05H04kbVqI-DjxAGpiviqR9A8VUA";
    // --- Fin de Reemplazos ---

    // Tus credenciales de prueba
    private static final String DEMO_EMAIL = "hernandezsalvadorneil@gmail.com";
    private static final String DEMO_PASSWORD = "password1234";

    public static void main(String[] args) {
        System.out.println("--- Iniciando Demo con SupabaseClient ---");

        try {
            // --- 1. CREAR LA CONFIGURACIÓN Y EL CLIENTE PRINCIPAL ---
            // Solo necesitamos crear *un* cliente, *una* vez.
            SupabaseConfig config = new SupabaseConfig.Builder(SUPABASE_URL, SUPABASE_ANON_KEY).build();
            SupabaseClient supabase = SupabaseClient.create(config);

            // --- 2. REGISTRAR EL USUARIO (signUp) ---
            // Nota: Si el usuario ya existe, esto fallará.
            // En ese caso, comenta signUp() y descomenta signInWithPassword()
            System.out.println("Paso 1: Creando usuario demo: " + DEMO_EMAIL);

            // Intenta crear el usuario
            supabase.auth().signUp(DEMO_EMAIL, DEMO_PASSWORD).join();
            System.out.println("¡Usuario creado! (Revisa tu email si tienes la confirmación activada)");

            // --- 3. INICIAR SESIÓN (signIn) ---
            // Ahora iniciamos sesión.
            // ¡EL CLIENTE SE AUTENTICARÁ INTERNAMENTE!
            System.out.println("Paso 2: Iniciando sesión...");
            AuthResponse authResponse = supabase.auth().signInWithPassword(DEMO_EMAIL, DEMO_PASSWORD).join();

            System.out.println("¡Login exitoso! Usuario: " + authResponse.getUser().getEmail());
            System.out.println("Paso 3: El cliente Postgrest ahora está autenticado internamente.");

            // --- 4. INSERTAR EN LA TABLA 'PRUEBA' ---
            // Ya no necesitamos crear un cliente nuevo.
            // Usamos el 'from()' del cliente principal.
            System.out.println("Paso 4: Intentando insertar en la tabla 'PRUEBA'...");

            // Preparamos los datos que especificaste
            Map<String, Object> dataToInsert = new HashMap<>();
            dataToInsert.put("id", System.currentTimeMillis()); // Un 'long' (int8) de ejemplo
            dataToInsert.put("created_at", Instant.now().toString()); // Un 'timestamptz'

            // Ejecutamos el insert
            // ¡Esto ya usa el token JWT automáticamente!
            String insertResult = supabase.from("PRUEBA")
                    .insert(dataToInsert)
                    .execute()
                    .join();

            System.out.println("--- ¡ÉXITO! ---");
            System.out.println("Insert realizado correctamente.");
            System.out.println("Respuesta del servidor: " + insertResult);

            // --- 5. CERRAR SESIÓN ---
            System.out.println("Paso 5: Cerrando sesión...");
            supabase.auth().signOut().join();
            System.out.println("Sesión cerrada. El cliente Postgrest vuelve a ser anónimo.");


        } catch (Exception e) {
            System.err.println("--- ¡ERROR! ---");
            System.err.println("La operación falló: " + e.getMessage());
            e.printStackTrace();
        }
    }
}