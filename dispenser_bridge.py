import requests

API_URL = "https://yharon.pythonanywhere.com"

oos_payload = {
    "dispenser_id": 1, #Different dispensers should have different IDs
    "description": "Dispenser out of service"
}

# >>> Call this method to add a notification and send a message on telegram to technicians
def notify_out_of_service():
    try:
        response = requests.post(API_URL + '/notifications', json=oos_payload)

        if response.status_code == 200:
            print("Notification added successfully!")
        else:
            print(f"Failed to add notification. Status code: {response.status_code}")
            print(response.json())

    except Exception as e:
        print(f"Error in the sender")

# >>> Call this method to send a fill record to the server
def post_fill(dispenser_id, bottle_id, fill_datetime):
    data = {
        'dispenser_id': dispenser_id,
        'bottle_id': bottle_id,
        'datetime': fill_datetime
    }

    response = requests.post(API_URL + 'fills', json=data)