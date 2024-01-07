import requests

API_URL = "https://yharon.pythonanywhere.com/notifications"

oos_payload = {
    "dispenser_id": 1, #Different dispensers should have different IDs
    "description": "Dispenser out of service"
}

# >>> Call this method to add a notification and send a message on telegram to technicians
def notify_out_of_service():
    try:
        response = requests.post(API_URL, json=oos_payload)

        if response.status_code == 200:
            print("Notification added successfully!")
        else:
            print(f"Failed to add notification. Status code: {response.status_code}")
            print(response.json())

    except Exception as e:
        print(f"Error in the sender")
