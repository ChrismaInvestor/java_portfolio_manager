from flask import Flask, request
import akshare as ak

app = Flask(__name__)

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

    return ans

@app.route('/minPrice/<code>', methods=['GET'])
def listMinPrices(code):
    stock_zh_a_minute_df = ak.stock_zh_a_minute(
        symbol=combineCode(code), period='1', adjust="qfq").dropna()
    ans = []
    for index, row in stock_zh_a_minute_df.iterrows():
        ans.append({"code": code[-6:], "price": row['close'],"volume": row['volume'], "time": row['day']})  
    return ans

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

if __name__=='__main__':
    app.run(debug=True)
