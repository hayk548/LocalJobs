const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.https.onCall(async (data, context) => {
  const token = data.token;
  const title = data.title;
  const body = data.body;
  const jobId = data.jobId;

  if (!token || !title || !body) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields');
  }

  const message = {
    notification: {
      title: title,
      body: body,
    },
    data: {
      jobId: jobId,
    },
    token: token,
  };

  try {
    await admin.messaging().send(message);
    return { success: true };
  } catch (error) {
    throw new functions.https.HttpsError('internal', 'Failed to send notification', error);
  }
});