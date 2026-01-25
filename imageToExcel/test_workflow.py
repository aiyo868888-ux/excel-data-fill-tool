"""
Test complete workflow: template + image -> output
"""
import requests
import os

# Web API endpoint
url = "http://localhost:5000/api/process"

# Files to upload
files = {
    'excel_file': open('中原证券进场材料统计表模板.xlsx', 'rb'),
    'image_files': open('test_image.jpg', 'rb'),
}

# Optional data
data = {
    'custom_data': '{"序号":"10","材料名称":"测试材料","数量":"100"}'
}

print("Testing workflow...")
print(f"URL: {url}")
print(f"Excel: 中原证券进场材料统计表模板.xlsx")
print(f"Image: test_image.jpg")
print(f"Custom data: {data['custom_data']}")

try:
    response = requests.post(url, files=files, data=data, timeout=120)
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response:\n{response.json()}")

    # Download result if successful
    if response.status_code == 200 and response.json().get('success'):
        download_url = "http://localhost:5000" + response.json()['download_url']
        print(f"\nDownloading result from: {download_url}")

        result_response = requests.get(download_url)
        output_file = "output_test.xlsx"
        with open(output_file, 'wb') as f:
            f.write(result_response.content)
        print(f"Saved to: {output_file}")

except Exception as e:
    print(f"Error: {e}")

finally:
    for f in files.values():
        f.close()
