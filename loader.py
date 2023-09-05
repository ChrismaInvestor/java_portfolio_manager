import akshare as ak
import requests
host = "http://localhost:8080"
url = host + "/price"

stock_list_df = ak.stock_info_a_code_name()

s = requests.Session()

for index, row in stock_list_df.iterrows():
    if (row['code'].startswith("0") | row['code'].startswith("3")):
        stock_zh_a_minute_df = ak.stock_zh_a_minute(
            symbol="sz" + row['code'], period='1', adjust="qfq")
        for index2, row2 in stock_zh_a_minute_df.iterrows():
            body = {"code": row['code'], "price": row2['close'],
                    "volume": row2['volume'], "time": row2['day']}
            
            s.post(url=url, json=body)
    if (row['code'].startswith("6")):
        stock_zh_a_minute_df = ak.stock_zh_a_minute(
            symbol="sh" + row['code'], period='1', adjust="qfq")
        for index2, row2 in stock_zh_a_minute_df.iterrows():
            body = {"code": row['code'], "price": row2['close'],
                    "volume": row2['volume'], "time": row2['day']}
            s.post(url=url, json=body)
    break
