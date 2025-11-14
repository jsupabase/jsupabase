# jsupabase: SDK de Java para Supabase

SDK de Java para Supabase. Construido con las librerías nativas del JDK 11 (LTS).

## 🎯 Finalidad del Proyecto

`jsupabase` es una iniciativa Open Source para construir un cliente de backend como servicio (BaaS) para Supabase utilizando las mejores prácticas de Java moderno.

**Nuestro objetivo principal es la Baja Latencia y la Eficiencia.** Hemos diseñado la arquitectura para ser completamente asíncrona, basándonos en el **HttpClient nativo de Java 11** para evitar dependencias de red externas pesadas, asegurando el máximo rendimiento en entornos de servidor (como Spring Boot o Jakarta EE).

---

## 🏗️ Arquitectura Modular (Proyecto Multi-Módulo)

El proyecto se estructura en **6 módulos de Gradle** con responsabilidades claras, siguiendo un flujo de dependencia estricto y unidireccional para prevenir problemas de acoplamiento.

### Stack Tecnológico

| Componente | Elección | Razón Arquitectónica |
| :--- | :--- | :--- |
| **Lenguaje/JDK** | Java 11+ (LTS) | Base mínima para usar el `java.net.http.HttpClient` asíncrono y de alto rendimiento. |
| **Build Tool** | Gradle | Facilita la gestión de dependencias complejas y la estructura de proyecto multi-módulo (es superior a Maven para este propósito). |
| **Serialización** | Jackson (`jackson-databind`) | Estándar de la industria para JSON. Rápido, robusto y se centraliza en `core/util/JsonUtil` para manejo de errores. |
| **Licencia** | Apache 2.0 + CLA | Modelo profesional para asegurar la propiedad intelectual del proyecto y fomentar la colaboración comunitaria. |

### 🧭 Estructura de Módulos y Flujo de Dependencias

La dependencia fluye desde la base (`core`) hacia las funcionalidades (`auth`, `postgrest`, etc.) y, finalmente, al punto de entrada (`client`).

| Módulo | Paquete | Responsabilidad Principal |
| :--- | :--- | :--- |
| **`core`** | `io.github.jsupabase.core` | **El Cimiento.** Clases base, configuración (`SupabaseConfig`), excepciones, y utilidades compartidas (`JsonUtil`). |
| **`client`** | `io.github.jsupabase.client` | **La Fachada/El Agregador.** El único punto de entrada para el usuario. Contiene la clase `SupabaseClient` que delega las llamadas. |
| **`postgrest`** | `io.github.jsupabase.postgrest` | **Módulo de la Base de Datos.** Implementa la API CRUD, Filtros, Modificadores, y RPC. |
| **`auth`** | `io.github.jsupabase.auth` | **Módulo de Autenticación.** Gestionará la API GoTrue (registro, login, sesiones JWT). |
| **`realtime`** | `io.github.jsupabase.realtime` | **Módulo de Tiempo Real.** Gestionará la conexión WebSocket para eventos de base de datos. |
| **`storage`** | `io.github.jsupabase.storage` | **Módulo de Almacenamiento.** Gestionará la subida, descarga y gestión de archivos. |

---

## 📐 Detalles de la Implementación por Módulo

### 1. `core` (El Cimiento)

* **Funcionalidad Principal:** Gestionar la configuración inmutable (`SupabaseConfig`), las excepciones base (`SupabaseException` - `RuntimeException` para async) y la serialización JSON (`JsonUtil`).
* **Pendiente:** Implementar la clase `HttpClientBase` para centralizar la conexión HTTP/2 y la inyección de cabeceras.

### 2. `client` (La Fachada)

* **Funcionalidad Principal:** Exponer los métodos de fábrica (`client.postgrest()`, `client.auth()`) al usuario.

### 3. `postgrest` (Base de Datos - **DEFINICIÓN COMPLETA**)

* **Patrón de Filtros:** Utiliza el **`PostgrestFilterBuilder`** abstracto para consolidar todos los filtros (`.eq()`, `.gt()`, `.textSearch()`, `.or()`, etc.) y eliminar la duplicación de código en `Select`, `Update` y `Delete`.
* **API Fluida:** Utiliza el patrón de "Herencia Genérica" (`protected abstract T self();`) para asegurar que el encadenamiento de métodos sea *type-safe* (ej: `.select().eq().limit()`).

| Tipo de Acción | Clases Principales | Funcionalidades Clave Implementadas |
| :--- | :--- | :--- |
| **Lectura (SELECT)** | `PostgrestSelectBuilder` | Filtros heredados, `.limit()`, `.offset()`, `.order()`, `.single()`, `.maybeSingle()`, `.csv()`, `.count()`, `.explain()`. |
| **Mutación (INSERT)** | `PostgrestInsertBuilder` | `.insert(data)`, `.returningRepresentation()`, `.onConflict()`, `.upsert()`. |
| **RPC** | `PostgrestRpcBuilder` | `.rpc(fn, args)`, `.select()` (para incrustación en la respuesta). |

---

## ⏭️ Próximos Pasos

1.  **Terminar Modificadores de `SELECT`:** Finalizar la implementación de la lógica de cabeceras (`Accept` y `Prefer`) en `PostgrestSelectBuilder` para `.single()`, `.maybeSingle()`, `.csv()`, y `.explain()`.
2.  **Implementar Cliente HTTP:** Crear la clase final de conexión `HttpClientBase` en el módulo `core`.
3.  **Módulo `Auth`:** Empezar con el módulo de Autenticación.