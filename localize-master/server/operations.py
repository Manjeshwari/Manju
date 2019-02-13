import firebase_admin
from firebase_admin import credentials, db

from config import firebase_credentials, firebaseio_url, uid
from localize import Localize


class Operations():
    def __init__(self):
        # Fetch the service account key JSON file contents
        cred = credentials.Certificate(firebase_credentials)

        # Initialize the app with a custom auth variable, limiting the server's access
        firebase_admin.initialize_app(cred, {
            "databaseURL": firebaseio_url,
            "databaseAuthVariableOverride": {
                "uid": uid
            }
        })
        self.localize = Localize()

    def update_device_locale(self):
        try:
            ref_user = db.reference('/user')
            users = ref_user.get()
            if users is None:
                return
            for user_id in users:
                location_data = db.reference(
                    "/user/{}/location".format(user_id)).get()
                device_data = db.reference("/device/{}".format(user_id)).get()
                if location_data is None:
                    return
                if device_data is None:
                    return
                self.localize.set_data(location_data)
                self.localize.set_user(device_data)
                locale = self.localize.locate()["locale"]
                db.reference("/device/{}/location".format(user_id)).set(locale)
            print()
        except Exception as e:
            print("ERROR:", e)

    def delete_device(self):
        ref = db.reference("/device")
        ref.delete()

    def delete_user(self):
        ref = db.reference("/user")
        ref.delete()
