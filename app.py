from flask import Flask, request
import akshare as ak

app = Flask(__name__)

def post_security(code, name, session):
    url = "http://localhost:8080/security"
    session.post(url=url, json={"code": code, "name": name})

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
        # post_security(row['code'], row['name'], s)
        ans.append({"code":row['code'], "name":row['name']})

    return ans

if __name__=='__main__':
    app.run(debug=True)
