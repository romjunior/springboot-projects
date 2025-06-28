import requests
import time

while True:
    try:
        response = requests.get("http://localhost:8080/cluster?message=teste")
        print(f"Status: {response.status_code}, Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")
    time.sleep(0.2)