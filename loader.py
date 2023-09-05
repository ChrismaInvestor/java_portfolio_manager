import akshare as ak
import requests
host = "http://localhost:8080"
url = host + "/price"

stock_list_df = ak.stock_info_a_code_name()

for index, row in stock_list_df.iterrows():
    # if (row['code'].startswith("0") | row['code'].startswith("3")):
    #     stock_zh_a_minute_df = ak.stock_zh_a_minute(
    #         symbol="sz" + row['code'], period='1', adjust="qfq")
    #     for index2, row2 in stock_zh_a_minute_df.iterrows():
    #         body = {"code": row['code'], "price": row2['close'],
    #                 "volume": row2['volume'], "time": row2['day']}
            
    #         try:
    #             r = requests.post(url=url, json=body)
    #         except:
    #             print("Something went wrong")
    #         finally:
    #             r.close()
    if (row['code'].startswith("6")):
        stock_zh_a_minute_df = ak.stock_zh_a_minute(
            symbol="sh" + row['code'], period='1', adjust="qfq")
        for index2, row2 in stock_zh_a_minute_df.iterrows():
            body = {"code": row['code'], "price": row2['close'],
                    "volume": row2['volume'], "time": row2['day']}
            r = requests.post(url=url, json=body)
            r.close()
# requests.post(url=rul, json=body)
