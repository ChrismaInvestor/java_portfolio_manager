from flask import Flask, request
import akshare as ak
import json

app = Flask(__name__)


@app.route('/bidAsk/<code>',methods=['GET'])
def home(code):
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
    #return stock_bid_ask
    return {'price': price, 'volume': volume}
    # return json.dumps({'price': price,
    #                    'volume': volume})

if __name__=='__main__':
    app.run(debug=True)
