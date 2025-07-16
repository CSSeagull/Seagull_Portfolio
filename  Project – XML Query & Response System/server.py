import socket
import pandas as pd
import xml.etree.ElementTree as ET

# Load employee dataset from directory.csv
df = pd.read_csv("directory.csv")

def parse_query(query):
    """
    Parse the XML query and extract filtering conditions.
    """
    conditions = []
    root = ET.fromstring(query)
    for condition in root.findall("condition"):
        column = condition.find("column").text.strip()
        value = condition.find("value").text.strip()
        conditions.append((column, value))
    return conditions

def filter_data(conditions):
    """
    Filter the dataset based on the conditions.
    """
    filtered_df = df.copy()
    for column, value in conditions:
        filtered_df = filtered_df[filtered_df[column].str.contains(value, case=False, na=False)]
    return filtered_df

def create_response(filtered_df):
    """
    Create an XML response with the filtered data.
    """
    result = ET.Element("result")
    status = ET.SubElement(result, "status")
    data = ET.SubElement(result, "data")

    if not filtered_df.empty:
        status.text = "success"
        for _, row in filtered_df.iterrows():
            row_elem = ET.SubElement(data, "row")
            ET.SubElement(row_elem, "name").text = row["Name"]
            ET.SubElement(row_elem, "title").text = row["Title"]
            ET.SubElement(row_elem, "email").text = row["Email"]
    else:
        status.text = "no results"
    
    return ET.tostring(result, encoding="unicode")

def start_server():
    """
    Start the server to handle client queries.
    """
    host = "127.0.0.1"
    port = 65432

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((host, port))
    server_socket.listen(5)
    print(f"Server started. Listening on {host}:{port}...")

    while True:
        conn, addr = server_socket.accept()
        print(f"Connection received from {addr}.")

        # Receive XML query from client
        query = conn.recv(1024).decode()
        print("Received query:")
        print(query)

        # Process the query
        conditions = parse_query(query)
        filtered_df = filter_data(conditions)
        response = create_response(filtered_df)

        # Send response back to client
        conn.sendall(response.encode())
        print("Response sent.")
        conn.close()

if __name__ == "__main__":
    start_server()
