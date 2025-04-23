const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.borrarEmpleado = functions.https.onCall(async (data, context) => {
  const uid = data.uid;
  if (!uid) {
    throw new functions.https.HttpsError("invalid-argument", "No UID provided");
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

    // Eliminar el documento del usuario
    batch.delete(admin.firestore().collection("usuarios").doc(uid));

    // Eliminar también del Auth si quieres
    await admin.auth().deleteUser(uid);

    await batch.commit();

    return { mensaje: "Empleado eliminado correctamente" };
  } catch (error) {
    console.error("Error al borrar empleado:", error);
    throw new functions.https.HttpsError("internal", "Fallo al eliminar empleado");
  }
});