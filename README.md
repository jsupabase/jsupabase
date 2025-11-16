# jsupabase: SDK de Java para Supabase

![Versión](https://img.shields.io/badge/versión-0.1.0-blue.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)
![Licencia](https://img.shields.io/badge/licencia-Apache%202.0-blue.svg)

SDK de Java para Supabase. Construido con las librerías nativas del **JDK 11 (LTS)** para un rendimiento de E/S asíncrono y sin bloqueo.

## 🎯 Finalidad del Proyecto

`jsupabase` es una iniciativa Open Source para construir un SDK para brindar a los usuarios la posibilidad de hacer uso de Supabase desde nuestros entornos Java.

**Nuestro objetivo principal es la Baja Latencia y la Eficiencia.**  
La arquitectura está diseñada para ser completamente asíncrona usando el **HttpClient nativo de Java 11**, evitando dependencias externas pesadas y maximizando el rendimiento en entornos productivos.

---

## 🏗️ Arquitectura Modular (Proyecto Multi-Módulo)

El proyecto está dividido en **6 módulos Gradle** con responsabilidades claras y un flujo de dependencias estrictamente unidireccional:

```
jsupabase/
├── client/          (La fachada 'SupabaseClient' que une todo)
├── core/            (El cimiento: Configuración, Red, Utilidades)
├── auth/            (Autenticación - GoTrue)
├── postgrest/       (Base de Datos - PostgREST)
├── storage/         (Próximo paso)
└── realtime/        (Próximo paso)
```

---

## 🧰 Stack Tecnológico

| Componente | Elección | Razón Arquitectónica |
|-----------|----------|----------------------|
| **Lenguaje/JDK** | Java 11+ (LTS) | Permite usar `java.net.http.HttpClient` asíncrono. |
| **Build Tool** | Gradle | Ideal para multi-módulos y rendimiento. |
| **Serialización** | Jackson | Estándar rápido y robusto para JSON. |
| **Licencia** | Apache 2.0 + CLA | Modelo profesional y compatible con OSS. |

---

## 📐 Detalles de Implementación por Módulo

### ✔ 1. `core` — COMPLETADO
- `SupabaseConfig` — Configuración inmutable (Builder Pattern)
- `HttpClientBase` — Motor HTTP/2 asíncrono con manejo unificado de errores
- `JsonUtil` — Serialización y deserialización centralizada con Jackson

### ✔ 2. `auth` — COMPLETADO
Cliente stateful que implementa **todo el flujo GoTrue**:

#### Funciones implementadas:
- `signUp(email, pass, options)`
- `signInWithPassword(email, pass)`
- `signInWithOtpEmail(email, options)`
- `signInWithOtpPhone(phone, options)`
- `verifyOtp(params)`
- `signInWithOAuth(provider, options)`
- `signInWithIdToken(credentials)`
- `signInAnonymously(options)`
- `exchangeCodeForSession(code, verifier)` (PKCE)
- `getUser(jwt)`
- `updateUser(attributes)`
- `signOut()`
- `refreshSession(refreshToken)`
- `resetPasswordForEmail(email)`
- Sistema **onAuthStateChange** (`SIGNED_IN`, `SIGNED_OUT`, `TOKEN_REFRESHED`)

Incluye DTOs completos (`AuthResponse`, `Session`, `User`, etc.) y enums (`OtpType`, `OAuthProvider`).

---

### ✔ 3. `postgrest` — COMPLETADO

#### API Fluida basada en Builders genéricos
- Filtros (`eq()`, `gt()`, `gte()`, `like()`, `textSearch()`, `or()`, etc.)
- Select: `.limit()`, `.offset()`, `.order()`, `.single()`, `.maybeSingle()`, `.csv()`, `.count()`, `.explain()`
- Insert: `.insert()`, `.onConflict()`, `.upsert()`
- Update: `.update()`
- Delete: `.delete()`
- RPC: `.rpc(fn, args)`

Tabla resumen:

| Acción | Clase | Funcionalidades |
|--------|--------|----------------|
| **SELECT** | `PostgrestSelectBuilder` | Filtros heredados, single, maybeSingle, csv, explain |
| **INSERT** | `PostgrestInsertBuilder` | insert, onConflict, upsert |
| **UPDATE** | `PostgrestUpdateBuilder` | update, returning |
| **DELETE** | `PostgrestDeleteBuilder` | delete, returning |
| **RPC** | `PostgrestRpcBuilder` | rpc, select embedding |

---

### ✔ 4. `client` — COMPLETADO

El módulo más importante:

- `SupabaseClient` es la **fachada oficial**
- Orquesta `auth` y `postgrest`
- **Actualiza automáticamente el PostgrestClient cuando cambia la sesión**
- Cuando ocurre `SIGNED_IN`:
    - Se crea un PostgrestClient **autenticado** con el JWT
- Cuando ocurre `SIGNED_OUT`:
    - Se regresa al cliente **anónimo**

---

## 🔗 Integración Automática de Auth + Postgrest

Flujo completo:

1. Creas un `SupabaseClient` (modo anónimo)
2. Llamas `supabase.auth().signInWithPassword()`
3. Auth dispara `SIGNED_IN`
4. El client intercepta el evento
5. Construye un PostgrestClient autenticado con `Authorization: Bearer <jwt>`
6. Cualquier `.from("tabla")` posterior usa RLS automáticamente

---

## 🚀 Ejemplo Completo

```java
SupabaseConfig config = new SupabaseConfig.Builder(SUPABASE_URL, SUPABASE_ANON_KEY).build();
SupabaseClient supabase = SupabaseClient.create(config);

// Listener de cambios de sesión
supabase.auth().onAuthStateChange((event, session) -> {
    System.out.println("EVENTO: " + event);
});

// Login
supabase.auth().signInWithPassword("test@example.com", "password123").join();

// Insert autenticado
String result = supabase.from("profiles")
                        .insert(Map.of("username", "TestUser"))
                        .execute()
                        .join();

System.out.println("Resultado: " + result);

// Logout
supabase.auth().signOut().join();
```

---

## ⏭️ Próximos Pasos

- **Módulo Storage**  
  Subida, descarga, gestión de buckets, políticas.

- **Módulo Realtime**  
  Cliente WebSocket con soporte de canales y presencia.

---

## 📄 Licencia

Licencia **Apache 2.0**.  
Contribuciones requieren firmar el **CLA**.

