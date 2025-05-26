# 📲 NeoFichaje

**NeoFichaje** es una aplicación Android desarrollada para facilitar el control horario, la gestión de empleados en empresas pequeñas y medianas.

## 🛠 Tecnologías utilizadas

- **Android Studio** – Entorno de desarrollo
- **Kotlin** – Lenguaje de programación
- **XML** – Diseño de interfaces
- **Firebase Authentication** – Registro y login de usuarios
- **Firestore** – Base de datos en la nube (NoSQL)
- **Firebase Cloud Storage** – Almacenamiento de archivos (PDF de contratos y nóminas)
- **Google Sign-In** – Inicio de sesión con cuenta Google
- **MaterialCalendarView** – Componente visual de calendario para seleccionar fechas

## 👥 Roles de usuario

- **Administrador / Empresario**: puede registrar empleados, subir contratos y nóminas, gestionar asistencia y aprobar solicitudes.
- **Empleado / Técnico**: puede fichar entradas y salidas, solicitar vacaciones y permisos, y ver sus documentos personales.

## 🧩 Funcionalidades

- 📅 Control horario con geolocalización
- ✍️ Registro y login seguro (con correo y Google)
- 👤 Gestión de perfil
- 🧾 Visualización y descarga de contratos y nóminas
- 🌴 Solicitudes de vacaciones y permisos
- 🔔 Notificaciones internas para empleados y administradores

## 🔄 Flujo general

1. El usuario se registra o inicia sesión (correo o Google)
2. Según el rol, accede a un panel diferente
3. El técnico puede fichar y solicitar vacaciones
4. El empresario puede visualizar, aprobar o rechazar solicitudes
5. Ambos pueden ver sus documentos en PDF

## 📂 Organización del código

- /app – Lógica principal de la app (actividades, fragments, modelos)
- /gradle – Configuraciones del proyecto
- firebase.json – Configuración de Firebase

## 📈 Progreso

Este proyecto ha sido desarrollado de manera individual, incluyendo todas las fases de análisis, diseño, programación, pruebas e implementación final.  
Puedes revisar los [commits](https://github.com/KarinaRojasDev/proyecto-neo-fichaje/commits/main) para ver la evolución detallada del trabajo.


## 👩‍💻 Autora

Desarrollado por **Karina Rojas** como proyecto final del segundo curso de Desarrollo de Aplicaciones Multiplataforma (DAM).
