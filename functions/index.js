const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendJobApplicationNotification = functions.firestore
    .document("applications/{applicationId}")
    .onCreate(async (snap, context) => {
      const application = snap.data();
      const creatorId = application.creatorId;
      const jobTitle = application.jobTitle;

      // Fetch creator's FCM token
      const userDoc = await admin.firestore()
          .collection("users")
          .doc(creatorId)
          .get();
      if (!userDoc.exists) {
        console.log("User not found");
        return null;
      }

      const fcmToken = userDoc.data().fcmToken;
      if (!fcmToken) {
        console.log("No FCM token for user");
        return null;
      }

      // Send notification
      const message = {
        notification: {
          title: "New Job Application",
          body: `Someone applied for your job: ${jobTitle}`,
        },
        token: fcmToken,
      };

      return admin.messaging().send(message)
          .then(() => console.log("Notification sent"))
          .catch((error) => console.error("Error:", error));
    });
