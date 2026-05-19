# Guía de Compilación y Uso: TechScoop (macOS y Windows)

Aplicación Android de noticias tecnológicas que consume la API de NewsAPI. Incluye pantalla de login con Firebase, feed reactivo con imágenes (Coil), favoritos guardados, búsqueda inteligente y preferencias de usuario que modifican la aplicación en tiempo real.

Esta guía detalla el proceso exacto, paso a paso, para clonar, configurar y compilar la aplicación **TechScoop** exitosamente, ya sea que utilices una computadora con **macOS** o **Windows**.

---

## 1. Requisitos Previos (Ambos Sistemas)

Antes de comenzar, asegúrate de tener instalados los siguientes programas:

1. **Android Studio**: Descarga la última versión (Ladybug o superior) desde [developer.android.com/studio](https://developer.android.com/studio).
2. **Git**: Para clonar el repositorio. (Descárgalo en [git-scm.com](https://git-scm.com/)).
3. **JDK 17**: Por defecto ya viene incluido en las versiones recientes de Android Studio.

---

## 2. Descargar el Proyecto

Abre tu terminal (Terminal en macOS o Símbolo del Sistema / Git Bash en Windows) y ejecuta:

```bash
git clone https://github.com/ethanPRS/techscoop.git
```

Mueve la carpeta descargada a un directorio de trabajo seguro (evita carpetas con espacios o caracteres especiales en el nombre).

---

## 3. Configuración Inicial en Android Studio

1. Abre **Android Studio**.
2. En la pantalla de bienvenida, selecciona **Open**.
3. Navega hasta la carpeta `techscoop` que acabas de clonar y presiona **Open** / **OK**.
4. Android Studio comenzará a descargar las dependencias de Gradle (esto puede tardar unos minutos). Verás una barra de carga en la parte inferior que dice *Syncing...*

---

## 4. Configurar la API Key de Noticias y Firebase

Para que la aplicación funcione y compile correctamente, necesita conectarse a los servicios de noticias y de autenticación.

### Paso 4.1: Configurar la API Key (gradle.properties)
1. En la raíz del proyecto abre el archivo **`gradle.properties`** (asegúrate de que sea el Global o el del Proyecto).
2. Añade esta línea al final del archivo con la **clave real del proyecto**:

```properties
NEWS_API_KEY=efbfd6ab4a48482bb78935d636f5b3f3
```

### Paso 4.2: Archivo google-services.json (Firebase)
El proyecto utiliza Firebase para el sistema de inicio de sesión de usuarios.
1. Solicita a tu equipo el archivo **`google-services.json`** o descárgalo de la consola de Firebase del proyecto.
2. Arrástralo y suéltalo directamente dentro de la carpeta `app/` de tu proyecto.

Una vez hechos estos dos pasos, haz clic en el ícono del "Elefantito" en la parte superior derecha de Android Studio que dice **Sync Project with Gradle Files**. La sincronización ahora debe terminar en verde (éxito).

---

## 5. Compilar y Ejecutar la Aplicación

Existen dos formas principales de compilar la aplicación: Usando la interfaz gráfica de Android Studio o usando la línea de comandos (Terminal).

### Método A: Desde Android Studio (Recomendado)

1. En la parte superior de Android Studio, busca el **Device Manager** (Administrador de Dispositivos).
2. Crea un Emulador (ej. Pixel 6 con API 34+) e inícialo, o conecta tu teléfono físico mediante un cable USB (asegúrate de tener activada la Depuración USB en sus ajustes).
3. Una vez que tu dispositivo aparezca seleccionado en el menú superior, presiona el botón verde de **Play (▶ Run 'app')** o usa el atajo:
   - **macOS:** `Ctrl + R`
   - **Windows:** `Shift + F10`
4. Android Studio compilará el código e instalará la aplicación en tu dispositivo automáticamente.

### Método B: Desde la Línea de Comandos (Terminal)

Si prefieres compilar el archivo `.apk` manualmente sin abrir el editor, abre la terminal directamente dentro de la carpeta raíz del proyecto (`techscoop/`).

#### 🍏 Instrucciones para macOS (o Linux)

1. Primero, asegúrate de que el archivo ejecutable tenga permisos:
   ```bash
   chmod +x gradlew
   ```
2. Ejecuta el comando de compilación:
   ```bash
   ./gradlew assembleDebug
   ```
3. Si el comando termina exitosamente (`BUILD SUCCESSFUL`), encontrarás el archivo de la aplicación (APK) listo para instalarse en la ruta:
   `app/build/outputs/apk/debug/app-debug.apk`

*Si tienes un dispositivo conectado, puedes compilar e instalar directamente en un solo paso con:* `./gradlew installDebug`

#### 🪟 Instrucciones para Windows

1. En Windows usaremos el archivo batch proporcionado por Gradle. En tu Símbolo del Sistema o PowerShell ejecuta:
   ```cmd
   gradlew.bat assembleDebug
   ```
2. Espera a que termine el proceso (`BUILD SUCCESSFUL`).
3. El archivo de la aplicación se generará en:
   `app\build\outputs\apk\debug\app-debug.apk`

*Si tienes un dispositivo conectado, instala directamente con:* `gradlew.bat installDebug`

---

## 6. Solución a Errores Comunes de Compilación

*   **Error:** `SDK location not found. Define location with sdk.dir in the local.properties file...`
    *   **Solución:** Ve a `File > Project Structure > SDK Location` en Android Studio y verifica que la ruta de tu SDK esté bien configurada. En Windows usualmente es `C:\Users\TU_USUARIO\AppData\Local\Android\Sdk` y en macOS `/Users/TU_USUARIO/Library/Android/sdk`.
*   **Error:** `Java 17 is required...`
    *   **Solución:** Android Studio Ladybug trae Java 17/21 por defecto. Ve a `Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle` y asegúrate de que el **Gradle JDK** esté apuntando a JDK 17 o superior.
*   **Las noticias no cargan (Blank screen) o lanza error "API Key no configurada"**
    *   **Solución:** Revisa que el Paso 4.1 esté correcto y que la línea `NEWS_API_KEY=efbfd6ab4a48482bb78935d636f5b3f3` esté en tu `gradle.properties`, luego presiona "Sync Project with Gradle Files".
*   **Error de Firebase (`File google-services.json is missing`)**
    *   **Solución:** Verifica que el archivo se llame exactamente `google-services.json` (sin números extras como `google-services(1).json`) y esté situado **dentro** de la carpeta `app`, no en la carpeta raíz.
