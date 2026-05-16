# TechScoop

Aplicación Android de noticias tecnológicas que consume la API de [NewsAPI](https://newsapi.org). Incluye pantalla de login, feed con imágenes, búsqueda con filtros (fuente, idioma, orden, periodo) y menú de perfil.

---

## Requisitos previos

Antes de empezar asegúrate de tener instalado:

1. **Android Studio** Hedgehog o superior (probado con Ladybug / 2024.x).
2. **JDK 17** (lo trae por defecto Android Studio).
3. **Android SDK** con `compileSdk = 36` y `minSdk = 24`.
4. Conexión a internet (la app llama a `https://newsapi.org`).
5. Una **API Key gratuita** de NewsAPI: regístrate en <https://newsapi.org/register> y copia tu key.

---

## Compilación paso a paso (desde el ZIP)

### 1. Descomprimir el proyecto

1. Descarga el archivo `TechScoop.zip`.
2. Haz clic derecho y selecciona **Extraer aquí / Extract All**.
3. Guarda la carpeta resultante en una ruta **sin espacios ni acentos**, por ejemplo:
   - Windows: `C:\AndroidProjects\TechScoop`
   - macOS / Linux: `~/AndroidStudioProjects/TechScoop`

### 2. Abrir el proyecto en Android Studio

1. Abre **Android Studio**.
2. Selecciona **File → Open...** (o **Open an Existing Project** desde la pantalla de bienvenida).
3. Navega hasta la carpeta `TechScoop` descomprimida y dale **OK**.
4. Espera a que termine el **Gradle Sync** (la barra de estado dice "Indexing..." / "Syncing...").
   - Si aparece un aviso para instalar el Android SDK 36 o herramientas faltantes, acepta y deja que se descarguen.

### 3. Configurar la API Key de NewsAPI

La app necesita tu API Key personal. Hay dos opciones:

#### Opción A — Recomendada (en `gradle.properties`)

1. En la raíz del proyecto abre el archivo **`gradle.properties`**.
2. Añade (o reemplaza) esta línea al final:
   ```properties
   NEWS_API_KEY=TU_API_KEY_AQUI
   ```
3. Sustituye `TU_API_KEY_AQUI` por la key que copiaste desde NewsAPI.
4. Guarda y vuelve a sincronizar (**File → Sync Project with Gradle Files**).

#### Opción B — Variable de entorno

Si prefieres no escribir la key en el archivo, define la variable `NEWS_API_KEY` en tu sistema operativo y reinicia Android Studio.

> Sin esta key la app mostrará el error **"API Key no configurada"**.

### 4. Verificar `local.properties`

El archivo `local.properties` se crea automáticamente al abrir el proyecto y apunta a tu SDK de Android. Si no existe, créalo en la raíz con:

```properties
sdk.dir=/ruta/a/tu/Android/Sdk
```

- Windows ejemplo: `sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk`
- macOS ejemplo: `sdk.dir=/Users/TU_USUARIO/Library/Android/sdk`

### 5. Conectar un dispositivo o crear un emulador

- **Emulador**: en Android Studio ve a **Device Manager → Create Device → Pixel 6** (o cualquiera con API 24+) y descarga la imagen del sistema.
- **Dispositivo físico**: activa **Opciones de desarrollador** y **Depuración USB**, conéctalo por USB y autoriza el equipo.

### 6. Compilar y ejecutar

#### Desde Android Studio

1. Selecciona el dispositivo en la barra superior.
2. Pulsa el botón ▶ **Run 'app'** (o `Shift + F10`).
3. Espera a que se instale el APK y se abra automáticamente.

#### Desde la línea de comandos (opcional)

Desde la carpeta del proyecto:

- **Windows (PowerShell / CMD)**:
  ```bash
  gradlew.bat assembleDebug
  ```
- **macOS / Linux**:
  ```bash
  ./gradlew assembleDebug
  ```

El APK firmado en modo debug quedará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo en un dispositivo conectado:
```bash
./gradlew installDebug
```

---

## Uso de la app

1. **Login** — escribe cualquier email y contraseña no vacíos y pulsa **Entrar**.
2. **Feed principal** — al entrar se cargan automáticamente las últimas noticias de TechCrunch con su imagen.
3. **Búsqueda** (icono de lupa en la toolbar) — busca por palabras clave y aplica filtros:
   - Fuente: TechCrunch, The Verge, Wired, Ars Technica, Engadget, Hacker News.
   - Idioma: Inglés / Español.
   - Orden: Más recientes / Relevancia / Popularidad.
   - Periodo: Cualquier fecha / 24h / Semana / Mes.
4. **Menú overflow** (⋮ en la toolbar) → **Perfil** o **Cerrar Sesión**.

---

## Problemas comunes

| Problema | Solución |
| --- | --- |
| `API Key no configurada` | Añade `NEWS_API_KEY=...` en `gradle.properties` y sincroniza. |
| `SDK location not found` | Configura `sdk.dir` en `local.properties` o reinstala el SDK desde Android Studio. |
| `Unsupported Java version` | Asegúrate de usar **JDK 17** (Settings → Build, Execution → Gradle → Gradle JDK). |
| Las imágenes no cargan | Verifica que el dispositivo tiene internet y que la noticia tiene `urlToImage`. |
| HTTP 426 / 429 desde NewsAPI | Plan gratuito limitado, espera unos minutos o usa otra key. |
| `minSdk` muy alto | El emulador debe ejecutar Android 7.0 (API 24) o superior. |

---

## Estructura del proyecto

```
app/src/main/
├── AndroidManifest.xml
├── java/com/estudiante/techscoop/
│   ├── LoginActivity.kt        ← Pantalla inicial
│   ├── MainActivity.kt         ← Feed principal con autocarga
│   ├── BusquedaActivity.kt     ← Búsqueda con filtros
│   ├── PerfilActivity.kt       ← Perfil del usuario
│   ├── APIService.kt           ← Endpoints de NewsAPI
│   ├── ArticleRepository.kt    ← Cliente Retrofit + lógica de red
│   ├── ArticleViewModel.kt     ← LiveData (news, error, loading)
│   ├── Article.kt              ← Modelos de datos
│   └── SearchFilters.kt        ← Filtros de búsqueda
└── res/
    ├── layout/                 ← XML de cada pantalla
    ├── menu/menu_main.xml      ← Menú de la toolbar
    └── drawable/               ← Iconos (búsqueda, placeholder)
```

---

## Stack técnico

- **Kotlin** 2.0.21
- **Retrofit** 2.11.0 + **Gson** — cliente HTTP y parseo JSON
- **OkHttp** 4.12.0 — transporte HTTP y logging
- **Coil** 2.6.0 — carga de imágenes
- **Coroutines** + **ViewModel** + **LiveData**
- **Hilt** 2.51.1 — DI (preparado)
- **Material Components** 1.13.0
- **ViewBinding** activado
