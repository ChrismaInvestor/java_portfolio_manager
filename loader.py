import akshare as ak
stock_zh_a_minute_df = ak.stock_zh_a_minute(symbol='sz000876', period='1', adjust="qfq")

import requests
host="http://localhost:8080"
url = host + "/price"
body={"code":"sh113658","price":115,"volume":7,"time":"2023-08-17 10:43:00"}
requests.post(url=rul, json=body)

stockList = ak.stock_info_a_code_name()

for index, row in df.iterrows():
    print(index, row['name'], row['age'])


