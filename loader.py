import akshare as ak
import requests
from datetime import datetime

def post_minute_price(combined_code, raw_code,s):
    url="http://localhost:8080/price"
    stock_zh_a_minute_df = ak.stock_zh_a_minute(
                symbol=combined_code, period='1', adjust="qfq")
    stock_zh_a_minute_df = stock_zh_a_minute_df.dropna()

    global today
    stock_zh_a_minute_df = stock_zh_a_minute_df[stock_zh_a_minute_df['day'].str.contains(today)]

    list=[]
    for index, row in stock_zh_a_minute_df.iterrows():
        list.append({"code": raw_code, "price": row['close'],
                        "volume": row['volume'], "time": row['day']})  
    s.post(url=url, json=list)

def post_security(code, name, session):
    url = "http://localhost:8080/security"
    session.post(url=url, json={"code":code, "name":name})


if __name__=='__main__':
    stock_list_df = ak.stock_info_a_code_name()
    s = requests.Session()
    today = datetime.now().strftime("%Y-%m-%d")

    for index, row in stock_list_df.iterrows():
        post_security(row['code'], row['name'], s)
        if (row['code'].startswith("0") | row['code'].startswith("3")):
            post_minute_price("sz" + row['code'], row['code'],s)
        elif (row['code'].startswith("6")):
            post_minute_price("sh" + row['code'], row['code'],s)
        elif (row['code'].startswith("4")|row['code'].startswith("8")):
            post_minute_price("bj" + row['code'], row['code'],s)