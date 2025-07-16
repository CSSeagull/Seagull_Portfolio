# NSSA220 Project – XML Query & Response System

## Overview

This project was developed as part of the NSSA220 coursework. It implements a simple client-server system that processes XML-based queries and responses, integrates a web scraper, and uses a CSV directory for data reference.

## Features

- **Client-Server Communication:** Exchange of XML queries and responses between client and server scripts.
- **Query & Response Handling:** Pre-defined XML files for simulating or testing interactions.
- **Web Scraper:** Script for gathering and processing data from external sources.
- **CSV Directory Integration:** Lookup or reference using `directory.csv`.

## Project Structure

```
project_NSSA220_Vladislav/
├── client.py           # Client-side script
├── server.py           # Server-side script
├── scrapper.py         # Web scraping tool
├── directory.csv       # CSV data directory
├── queries/            # Folder with XML query files
│   ├── query1.xml
│   └── ...
├── responses/          # Folder with XML response files
│   ├── response1.xml
│   └── ...
```

## Getting Started

### Prerequisites

- Python 3.x
- (Recommended) Virtual environment

```bash
python3 -m venv venv
source venv/bin/activate
```

### Installation

If your code uses external Python packages, install them with:

```bash
pip install -r requirements.txt
```
*(Create `requirements.txt` if needed)*

### Usage

**Start the server:**
```bash
python server.py
```

**Run the client:**
```bash
python client.py
```

**Run the scraper:**
```bash
python scrapper.py
```

## Data Files

- `directory.csv` — Structured data for lookups or scraping.
- `queries/` — XML query files for simulated requests.
- `responses/` — XML response files for simulated replies.

## Notes

- Ensure all XML files are valid and placed in the correct folders.
- Extend or modify the client and server logic as needed for your use case.

## License

This project is for academic use. Please add a license if you intend to share or reuse.

---

**Author:** Vladislav  
**Course:** NSSA220