# Google Sign-In para todo el equipo

## Por qué no basta con poner una “clave” en el código

Google Sign-In **no** usa un texto que pongas en `LoginActivity`. Comprueba que el APK esté firmado con un certificado cuyo **SHA-1** esté registrado en **Firebase Console** para el paquete `com.juanpabloramos.techscoop`.

Cada PC, por defecto, firma el debug con su propio `~/.android/debug.keystore` → **SHA-1 distinto** → Google falla (códigos 10, 12501, etc.) salvo que registres cada huella en Firebase.

## Solución del proyecto: keystore compartido

En `app/` hay:

| Archivo | Uso |
|---------|-----|
| `team-debug.keystore` | Certificado **único** para builds debug de todos |
| `team-keystore.properties` | Alias y contraseñas (solo debug académico) |

`app/build.gradle.kts` hace que **debug** siempre firme con ese keystore. Así **profesor, compañeros y tú** generan el **mismo SHA-1**.

### SHA-1 del keystore del equipo (registrar en Firebase)

```
C3:0C:63:43:E8:0C:F7:1C:79:E6:2E:78:03:39:B5:C3:94:A9:8D:E9
```

Sin dos puntos (formato Firebase): `c30c6343e80cf71c79e62e780339b5c394a98de9`

SHA-256 (opcional en consola): `f6b61474aa7ee5f1f5fe77995478df844f104e2ce32a7193b93401d8e8332f0`

## Pasos en Firebase (una vez, quien tenga acceso al proyecto)

1. [Firebase Console](https://console.firebase.google.com/) → proyecto **techscoop-ea17c**.
2. **Configuración del proyecto** → app Android `com.juanpabloramos.techscoop`.
3. **Agregar huella digital** → pegar el SHA-1 de arriba (puedes dejar la huella antigua si quieres).
4. **Authentication** → **Sign-in method** → activar **Google** y guardar.
5. Descargar el nuevo **`google-services.json`** y reemplazar `app/google-services.json`.
6. **Sync Project with Gradle Files** y reinstalar la app en el dispositivo.

El `default_web_client_id` lo genera el plugin de Google Services desde ese JSON; no hace falta copiarlo a mano en `strings.xml`.

## Qué hace cada desarrollador

1. Clonar / actualizar el repo (debe incluir `app/team-debug.keystore` y `team-keystore.properties`).
2. Abrir el proyecto y **Run** en modo **debug** (no hace falta configurar SHA-1 local).
3. En login, pulsar **Continuar con Google**.

Si falla tras actualizar Firebase: desinstalar la app del teléfono/emulador e instalar de nuevo.

## Release / APK para entregar

El build **release** sigue usando la firma por defecto (otro SHA-1). Para un APK release con Google:

- Registrar también el SHA-1 del keystore de **release**, o  
- Firmar release con el mismo `team-debug.keystore` solo para demos (no recomendado en producción real).

## Errores frecuentes

| Síntoma | Causa |
|---------|--------|
| Error Google **10** | SHA-1 del APK no está en Firebase o `google-services.json` viejo |
| **12501** / cancelado | Cuenta Google del emulador no configurada o OAuth mal enlazado |
| “No se obtuvo token” | Falta `requestIdToken` con el Web Client ID (ya está en el código) |

## Código ya implementado

- `LoginActivity`: `GoogleSignInOptions` + `requestIdToken(default_web_client_id)` + Firebase `GoogleAuthProvider`.
- Dependencias: `play-services-auth`, `firebase-auth`.
- Plugin: `com.google.gms.google-services` en `app/build.gradle.kts`.
