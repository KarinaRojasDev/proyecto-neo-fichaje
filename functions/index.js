const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.borrarEmpleado = functions.https.onCall(async (data, context) => {
  console.log("Datos recibidos en borrarEmpleado:", data);
  const uid = data.uid;



  if (!uid) {
    throw new functions.https
    .HttpsError("invalid-argument", "UID no proporcionado");
  }

  const subcolecciones = [
    "contrato",
    "controlHorario",
    "nominas",
    "permisos_bajas",
    "vacaciones",
  ];

  try {
    const batch = admin.firestore().batch();

    for (const col of subcolecciones) {
      const docsSnap = await admin.firestore()
        .collection("usuarios")
        .doc(uid)
        .collection(col)
        .get();

      docsSnap.forEach((doc) => {
        batch.delete(doc.ref);
      });
    }

    batch.delete(admin.firestore().collection("usuarios").doc(uid));
    await admin.auth().deleteUser(uid);
    await batch.commit();

    return { mensaje: "Empleado eliminado correctamente" };
  } catch (error) {
    console.error("Error al borrar empleado:", error);
    throw new functions.https.HttpsError("internal", "Error al eliminar el empleado");
  }
});
