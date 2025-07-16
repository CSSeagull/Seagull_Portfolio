import socket
import xml.etree.ElementTree as ET

def send_query(query_file):
    """
    Send an XML query to the server and display the response.
    """
    host = "127.0.0.1"
    port = 65432

    # Read the query from the file
    with open(query_file, "r") as file:
        query = file.read()

    # Connect to the server and send the query
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((host, port))
    client_socket.sendall(query.encode())
    print(f"Sent query from {query_file} to server.")

    # Receive the response from the server
    response = ""
    while True:
        chunk = client_socket.recv(4096).decode()
        if not chunk:
            break
        response += chunk

    print("Received response:")
    print(response)

    # Parse and display the response
    root = ET.fromstring(response)
    status = root.find("status").text
    print(f"Status: {status}")

    if status == "success":
        print("Filtered Data:")
        for row in root.find("data").findall("row"):
            name = row.find("name").text
            title = row.find("title").text
            email = row.find("email").text
            print(f"Name: {name}, Title: {title}, Email: {email}")
    else:
        print("No results found.")

    client_socket.close()

if __name__ == "__main__":
    # Specify the query file to use
    send_query("queries/query1.xml")
