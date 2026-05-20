================================================================================
5.1. README — TechScoop
================================================================================

NOMBRE DE LA APP Y DEL EQUIPO
--------------------------------------------------------------------------------
  App:        TechScoop
  Paquete:    com.juanpabloramos.techscoop
  Repositorio: https://github.com/ethanPRS/techscoop

  Equipo:
    - Juan Pablo Ramos
    - Ethan Rivera
    - Santiago López Cerón
    - Christopher Reeker

  Descripción: aplicación Android de noticias tecnológicas (NewsAPI), con
  autenticación Firebase (email/contraseña y Google Sign-In), favoritos,
  búsqueda, preferencias de idioma/categoría y notificaciones locales.


VERSIÓN MÍNIMA DE ANDROID / API LEVEL
--------------------------------------------------------------------------------
  minSdk:     24  (Android 7.0 Nougat, API 24)
  targetSdk:  35  (Android 15, API 35)
  compileSdk: 35

  Recomendado para pruebas: emulador o dispositivo con API 24 o superior.
  Notificaciones en primer plano (Android 13+): requiere permiso POST_NOTIFICATIONS
  concedido en tiempo de ejecución.


INSTRUCCIONES PARA COMPILAR Y EJECUTAR
--------------------------------------------------------------------------------
  1) Requisitos
     - Android Studio (Ladybug o superior recomendado)
     - JDK 17+ (incluido con Android Studio)
     - Git

  2) Clonar el proyecto
       git clone https://github.com/ethanPRS/techscoop.git
       cd techscoop

  3) Configurar local.properties (NO se sube a Git)
     - Copiar la plantilla:
         cp local.properties.example local.properties
       (En Windows: copiar local.properties.example como local.properties)

     - Editar local.properties y completar:

         sdk.dir=RUTA_A_TU_ANDROID_SDK
         NEWS_API_KEY=TU_CLAVE_DE_NEWSAPI

     - Obtener API key gratuita en: https://newsapi.org
     - Ejemplo sdk.dir (Windows):
         sdk.dir=C:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

     La clave se inyecta en BuildConfig al compilar (ver app/build.gradle.kts).
     Tras cambiar la key: File → Sync Project with Gradle Files → Rebuild.

  4) Firebase (login obligatorio para usar la app)
     - El archivo app/google-services.json debe existir en el repo o
       descargarse desde Firebase Console (proyecto del equipo).
     - Sin este archivo la compilación falla o el login no funciona.

  5) Abrir en Android Studio
     - Open → carpeta techscoop → esperar Gradle Sync (barra inferior).

  6) Ejecutar
     - Crear/iniciar emulador (API 24+) o conectar dispositivo con depuración USB.
     - Run ▶ (app) o atajo: Windows Shift+F10 / macOS Ctrl+R.

  7) Compilar APK por terminal (opcional)
     - Windows:  gradlew.bat assembleDebug
     - macOS/Linux: ./gradlew assembleDebug
     - APK: app/build/outputs/apk/debug/app-debug.apk
     - Instalar en dispositivo: gradlew installDebug


CREDENCIALES DE PRUEBA (LOGIN)
--------------------------------------------------------------------------------
  La app usa Firebase Authentication. No hay usuario/contraseña fijos en el
  código del repositorio.

  Opciones para probar:

    A) Crear cuenta nueva en la app
       - Pantalla inicial → "Regístrate" → correo, contraseña (mín. 6 caracteres).

    B) Inicio con Google
       - Botón "Continuar con Google" (cuenta Gmail del evaluador).

    C) Cuenta ya registrada por el equipo
       - Usar el correo y contraseña que hayan creado en Firebase durante el
         desarrollo (solicitar al equipo si es para evaluación).

  Recuperar contraseña: en login → "¿Olvidaste tu contraseña?" → Firebase envía
  enlace al correo.


CONFIGURACIÓN ESPECIAL NECESARIA
--------------------------------------------------------------------------------
  • NEWS_API_KEY en local.properties (obligatorio para ver noticias).

  • google-services.json dentro de app/ (obligatorio para Firebase Auth y Google).

  • Google Sign-In en equipo (mismo SHA-1 para todos):
    - El proyecto firma debug con team-debug.keystore (app/team-debug.keystore).
    - Credenciales en app/team-keystore.properties (incluido en el repo).
    - El SHA-1 del keystore del equipo debe estar registrado en Firebase Console.
    - Detalle completo: docs/GOOGLE_SIGNIN_EQUIPO.md

  • Si cambió el keystore o Firebase: desinstalar la app del dispositivo y
    volver a instalar (evita INSTALL_FAILED_UPDATE_INCOMPATIBLE).

  • Conexión a Internet necesaria (noticias, login y pantalla offline sin red).

  • Permiso de notificaciones (Android 13+): la app lo solicita al abrir MainActivity.

  • Documentación ampliada de compilación: README.md (guía paso a paso macOS/Windows).

================================================================================
