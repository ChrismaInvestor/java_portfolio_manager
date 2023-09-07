import akshare as ak
import requests
from datetime import datetime

def post_minute_price(combined_code, raw_code,s):
    stock_zh_a_minute_df = ak.stock_zh_a_minute(
                symbol=combined_code, period='1', adjust="qfq")
    stock_zh_a_minute_df = stock_zh_a_minute_df.dropna()
    list=[]
    for index, row in stock_zh_a_minute_df.iterrows():
        list.append({"code": raw_code, "price": row['close'],
                        "volume": row['volume'], "time": row['day']})  
    s.post(url=url, json=list)


if __name__=='__main__':
    host = "http://localhost:8080"
    url = host + "/price"

    stock_list_df = ak.stock_info_a_code_name()

    print(stock_list_df.shape[0])

    s = requests.Session()

    for index, row in stock_list_df.iterrows():
        if (row['code'].startswith("0") | row['code'].startswith("3")):
            post_minute_price("sz" + row['code'], row['code'],s)
            # stock_zh_a_minute_df = ak.stock_zh_a_minute(
            #     symbol="sz" + row['code'], period='1', adjust="qfq")
            # stock_zh_a_minute_df = stock_zh_a_minute_df.dropna()
            # list=[]
            # for index2, row2 in stock_zh_a_minute_df.iterrows():
            #     list.append({"code": row['code'], "price": row2['close'],
            #             "volume": row2['volume'], "time": row2['day']})
            # s.post(url=url, json=list)
        elif (row['code'].startswith("6")):
            post_minute_price("sh" + row['code'], row['code'],s)
            # stock_zh_a_minute_df = ak.stock_zh_a_minute(
            #     symbol="sh" + row['code'], period='1', adjust="qfq")
            # stock_zh_a_minute_df = stock_zh_a_minute_df.dropna()
            # list=[]
            # for index2, row2 in stock_zh_a_minute_df.iterrows():
            #     list.append({"code": row['code'], "price": row2['close'],
            #             "volume": row2['volume'], "time": row2['day']})  
            # s.post(url=url, json=list)
        elif (row['code'].startswith("4")|row['code'].startswith("8")):
            post_minute_price("bj" + row['code'], row['code'],s)
            # stock_zh_a_minute_df = ak.stock_zh_a_minute(
            #             symbol="bj" + row['code'], period='1', adjust="qfq")
            # stock_zh_a_minute_df = stock_zh_a_minute_df.dropna()
            # list=[]
            # for index2, row2 in stock_zh_a_minute_df.iterrows():
            #     list.append({"code": row['code'], "price": row2['close'],
            #                     "volume": row2['volume'], "time": row2['day']})  
            # s.post(url=url, json=list)
