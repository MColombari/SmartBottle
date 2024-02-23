import requests
import serial
import serial.tools.list_ports
import parse
from datetime import datetime

API_URL = "https://yharon.pythonanywhere.com"
WATER_WARNING_THREASHOLD = 10
DISPENCER_ID = 1

oos_payload = {
    "dispenser_id": DISPENCER_ID, #Different dispensers should have different IDs
    "description": "Dispenser out of service"
}

class Bridge:
    def __init__(self, port_name):
        self.port_name = port_name
        self.connect_to_arduino()

    def connect_to_arduino(self):
        self.ser = None
        try:
            if self.port_name is not None:
                print ("connecting to " + self.port_name)
                self.ser = serial.Serial(self.port_name, 9600, timeout=10)
        except:
            print("Error Connection")
            self.ser = None
        self.inbuffer = []

    def loop(self):
		# infinite loop for serial managing
		#
        while (True):
			# look for a byte from serial
            if not self.ser is None:
                if self.ser.in_waiting>0:
					# data available from the serial port
                    lastchar = self.ser.read(1)

                    if lastchar == b'/':
                        # Start again
                        self.inbuffer = []
                    
                    if lastchar==b'\xfe': #EOL
                        print("Message received")
                        self.useData()
                        self.inbuffer =[]
                    else:
						# append
                        self.inbuffer.append (lastchar)

    def useData(self):
        in_message = "".join([x.decode('utf-8') for x in self.inbuffer])
        print(in_message)
        format_string = '/{}-{}'
        parsed = parse.parse(format_string, in_message)
        bottle_id = int(parsed[0], 16)
        water_level = int(parsed[1], 10)
        print(f"Bottle ID: {bottle_id}\nWater level: {water_level}\n")

        if water_level < WATER_WARNING_THREASHOLD:
            notify_out_of_service()
        
        current_time = str(datetime.now().strftime("%H:%M:%S"))
        post_fill(DISPENCER_ID, 111, current_time)


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



if __name__ == '__main__':
    Bridge('/dev/cu.usbserial-143230').loop()