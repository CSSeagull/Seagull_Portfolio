from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from bs4 import BeautifulSoup
import pandas as pd

# Setup Selenium WebDriver
driver = webdriver.Chrome()  # Ensure ChromeDriver is installed and in PATH
driver.get("https://www.rit.edu/dubai/directory")

# Wait for the page to load initially
time.sleep(2)

# Handle the cookie consent banner by closing it (if it exists)
try:
    cookie_accept_button = WebDriverWait(driver, 10).until(
        EC.element_to_be_clickable((By.CSS_SELECTOR, ".cookie-consent--wrapper button"))
    )
    cookie_accept_button.click()
    print("Cookie consent banner closed.")
except Exception as e:
    print("No cookie consent banner found or already closed.")

# Click 'Load More' button 5 times
for _ in range(5):
    try:
        load_more_button = WebDriverWait(driver, 10).until(
            EC.element_to_be_clickable((By.CLASS_NAME, "see-more"))
        )
        driver.execute_script("arguments[0].scrollIntoView();", load_more_button)
        load_more_button.click()
        time.sleep(2)
        print("Clicked 'Load More' button.")
    except Exception as e:
        print(f"Error clicking 'Load More' button: {e}")
        break

# Get page source after loading all content
page_source = driver.page_source
driver.quit()

# Parse HTML using BeautifulSoup
soup = BeautifulSoup(page_source, "html.parser")

# List to store employee details
employees = []

# Select employee cards (Refined based on the structure)
employee_cards = soup.select('.row.position-relative .view-content article')  # Targeting general structure for employee cards
print(f"Found {len(employee_cards)} employee cards.")  # Debugging line

# Loop through each employee card to extract data
for idx, item in enumerate(employee_cards, start=1):
    # Extract name
    name_tag = item.select_one("div.col-xs-12.col-sm-5.person--info > div:nth-child(1) > a")
    name = name_tag.get_text(strip=True) if name_tag else "N/A"

    # Extract title
    title_tag = item.select_one("div.col-xs-12.col-sm-5.person--info > div:nth-child(2)")
    title = title_tag.get_text(strip=True) if title_tag else "N/A"

    # Extract email
    email_tag = item.select_one("a[href^='mailto:']")
    email = email_tag['href'].replace("mailto:", "") if email_tag else "N/A"

    # Add employee details to the list
    employees.append({'Name': name, 'Title': title, 'Email': email})

# Save to CSV
df = pd.DataFrame(employees)
df.to_csv("directory.csv", index=False)

# Print success message and sample data
if not df.empty:
    print(f"Data successfully saved to directory.csv with {len(df)} entries.")
    print(df.head())
else:
    print("No data found. Please check the selectors.")