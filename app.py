from flask import Flask
import akshare as ak
import time
from xtquant import xtdata
from xtquant import xtconstant
from xtquant.xttrader import XtQuantTrader
from xtquant.xttype import StockAccount

app = Flask(__name__)

xt_trader = XtQuantTrader(r"C:\国金QMT交易端模拟\userdata_mini", int(time.time()))
xt_trader.start()
print(xt_trader.connect())
account = StockAccount("55003182")

@app.route("/buy/<code>/<price>/<vol>", methods=["GET"])
def buy(code, price, vol):
    order_id = xt_trader.order_stock(
        account,
        combineCodeGuojin(code),
        xtconstant.STOCK_BUY,
        int(vol),
        xtconstant.FIX_PRICE,
        float(price),
    )
    return str(order_id)


@app.route("/sell/<code>/<price>/<vol>", methods=["GET"])
def sell(code, price, vol):
    order_id = xt_trader.order_stock(
        account,
        combineCodeGuojin(code),
        xtconstant.STOCK_SELL,
        int(vol),
        xtconstant.FIX_PRICE,
        float(price),
    )
    return str(order_id)


@app.route("/bidAsk/<codes>", methods=["GET"])
def bidAsk(codes):
    code_list = codes.split(",")
    for index in range(len(code_list)):
        code_list[index] = combineCodeGuojin(code_list[index])

    ticks = xtdata.get_full_tick(code_list)
    ans = []
    for key, value in ticks.items():
        askVol = int(value["askVol"][0])
        bidVol = int(value["bidVol"][0])
        askPrice = value["askPrice"][0]
        bidPrice = value["bidPrice"][0]
        askPrice2 = value["askPrice"][1]
        bidPrice2 = value["bidPrice"][1]
        askVol2 = int(value["askVol"][1])
        bidVol2 = int(value["bidVol"][1])
        if askVol == 0:
            askVol = bidVol
            askPrice = round(bidPrice * 0.99, 2)
        if bidVol == 0:
            bidVol = askVol
            bidPrice = round(askPrice * 1.01, 2)
        multiple = 100 if not (key.startswith("11") or key.startswith("12")) else 10
        ans.append(
            {
                "securityCode": key.split(".")[0],
                "askPrice1": askPrice,
                "askVol1": askVol * multiple,
                "bidPrice1": bidPrice,
                "bidVol1": bidVol * multiple,
                "lastPrice": value["lastPrice"],
                "lastClose": value["lastClose"],
                "askVol2": askVol2 * multiple,
                "askPrice2": askPrice2,
                "bidVol2": bidVol2 * multiple,
                "bidPrice2": bidPrice2
            }
        )
    return ans


@app.route("/position/<code>", methods=["GET"])
def checkPosition(code):
    position = xt_trader.query_stock_position(account, combineCodeGuojin(code))
    if position is not None and position.volume != 0:
        return {
            "unitCost": round(position.open_price, 3),
            "vol": int(position.volume),
            "marketValue": round(position.market_value, 2),
        }
    return {}


@app.route("/todayTrades", methods=["GET"])
def listTodayTrades():
    trades = xt_trader.query_stock_trades(account)
    ans = []
    for trade in trades:
        if trade.order_type == 24:
            trade.traded_amount = trade.traded_amount * (-1)
        ans.append(
            {
                "amount": round(trade.traded_amount, 2),
                "orderId": trade.order_id,
                "securityCode": trade.stock_code.split(".")[0],
            }
        )
    return ans


@app.route("/minPrice/<codes>", methods=["GET"])
def minPrices(codes):
    code_list = codes.split(",")
    for index in range(len(code_list)):
        code_list[index] = combineCodeGuojin(code_list[index])

    minPrices = xtdata.get_local_data(
        stock_list=code_list, period="1m", start_time="20231101", end_time="20231204"
    )
    ans = []
    for key, value in minPrices.items():
        if value.empty:
            xtdata.download_history_data2(
                stock_list=[key],
                period="1m",
                start_time="20231101",
                end_time="20231204",
            )
    #     code = key.split(".")[0]
    #     for index, row in value.iterrows():
    #         time = datetime.strptime(str(index), '%Y%m%d%H%M%S')
    #         ans.append({"code": code, "price": row['close'],"volume": row['volume'], "time": time.strftime("%Y-%m-%d %H:%M:%S")})
    return "success"


@app.route("/stocks", methods=["GET"])
def listStocks():
    stock_list_df = ak.stock_info_a_code_name()
    ans = []
    for index, row in stock_list_df.iterrows():
        ans.append({"code": row["code"], "name": row["name"]})

    cov_list_df = ak.bond_cov_comparison()

    for index, row in cov_list_df.iterrows():
        ans.append({"code": row["转债代码"], "name": row["转债名称"]})

    return ans


def combineCodeGuojin(code):
    if code.startswith("688"):
        return code + ".SH"
    if code.startswith("0") | code.startswith("3"):
        return code + ".SZ"
    if code.startswith("6"):
        return code + ".SH"
    if code.startswith("4") | code.startswith("8"):
        return code + "BJ"
    if code.startswith("11"):
        return code + ".SH"
    if code.startswith("12"):
        return code + ".SZ"
    return code


if __name__ == "__main__":
    app.run(threaded=True, debug=True)
