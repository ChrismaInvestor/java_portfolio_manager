from flask import Flask
import akshare as ak
from datetime import datetime
import time
from threading import Lock
from xtquant import xtdata

app = Flask(__name__)

app.config['current_time'] = datetime.now()

lock = Lock()

@app.route('/bidAsk/<codes>', methods=['GET'])
def bidAsk(codes):
    code_list = codes.split(",")
    for index in range(len(code_list)):
        code_list[index] = combineCodeGuojin(code_list[index])
    
    ticks = xtdata.get_full_tick(code_list)
    ans = []
    for key, value in ticks.items():
        ans.append({"securityCode": key.split(".")[0], "askPrice1":value['askPrice'][0], "askVol1":value['askVol'][0],"bidPrice1":value['bidPrice'][0], "bidVol1":value['bidVol'][0],"lastPrice":value['lastPrice']})
    print(ans)
    return ans

@app.route('/minPrice/<codes>', methods=['GET'])
def minPrices(codes):
    code_list = codes.split(",")
    for index in range(len(code_list)):
        code_list[index] = combineCodeGuojin(code_list[index])

    minPrices = xtdata.get_local_data(stock_list=code_list, period='1m', start_time='20231101', end_time='20231204')
    ans = []
    for key, value in minPrices.items():
        if value.empty:
            xtdata.download_history_data2(stock_list=[key], period='1m', start_time='20231101', end_time='20231204')
    #     code = key.split(".")[0]
    #     for index, row in value.iterrows():
    #         time = datetime.strptime(str(index), '%Y%m%d%H%M%S')
    #         ans.append({"code": code, "price": row['close'],"volume": row['volume'], "time": time.strftime("%Y-%m-%d %H:%M:%S")})
    return "success"

@app.route('/bidAsk/buy/<code>',methods=['GET'])
def buy(code):
    stock_bid_ask = ak.stock_bid_ask_em(
                code)
    stock_bid_ask_sell1=stock_bid_ask[stock_bid_ask['item'].str.startswith('sell_1')]
    price = None
    volume = None
    for index, row in stock_bid_ask_sell1.iterrows():
        if(row['item']== 'sell_1'):
            price = row['value']
        if(row['item']== 'sell_1_vol'):
            volume = row['value']
    return {'price': price, 'volume': volume}

@app.route('/bidAsk/sell/<code>',methods=['GET'])
def sell(code):
    stock_bid_ask = ak.stock_bid_ask_em(
                code)
    stock_bid_ask_buy1=stock_bid_ask[stock_bid_ask['item'].str.startswith('buy_1')]
    price = None
    volume = None
    for index, row in stock_bid_ask_buy1.iterrows():
        if(row['item']== 'buy_1'):
            price = row['value']
        if(row['item']== 'buy_1_vol'):
            volume = row['value']
    return {'price': price, 'volume': volume}


@app.route('/stocks', methods=['GET'])
def listStocks():
    stock_list_df = ak.stock_info_a_code_name()
    ans = []
    for index, row in stock_list_df.iterrows():
        ans.append({"code":row['code'], "name":row['name']})

    cov_list_df = ak.bond_cov_comparison()

    for index, row in cov_list_df.iterrows():
        ans.append({"code":row['转债代码'], "name":row['转债名称']})

    return ans

# @app.route('/minPrice/<code>', methods=['GET'])
# def listMinPrices(code):
#     while (datetime.now() - app.config['current_time']).total_seconds() <1.5:
#         time.sleep(1.5)
#     with lock:
#         app.config['current_time'] = datetime.now()
#     stock_zh_a_minute_df = ak.stock_zh_a_minute(
#         symbol=combineCode(code), period='1', adjust="qfq").dropna()
#     ans = []
#     for index, row in stock_zh_a_minute_df.iterrows():
#         ans.append({"code": code[-6:], "price": row['close'],"volume": row['volume'], "time": row['day']})
#     return ans

def combineCode(code):
    if (code.startswith("688")):
        return "sh" + code
    if (code.startswith("0") | code.startswith("3")):
        return "sz" + code
    if (code.startswith("6")):
        return "sh" + code
    if (code.startswith("4")|code.startswith("8")):
        return "bj" + code
    return code


def combineCodeGuojin(code):
    if (code.startswith("688")):
        return code+".SH"
    if (code.startswith("0") | code.startswith("3")):
        return code+".SZ"
    if (code.startswith("6")):
        return code+".SH"
    if (code.startswith("4")|code.startswith("8")):
        return code+"BJ"
    if (code.startswith("11")):
        return code+".SH"
    if (code.startswith("12")):
        return code+".SZ"
    return code

if __name__=='__main__':
    app.run(debug=True)
