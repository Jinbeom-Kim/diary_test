<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript">
	var N = 10;
	var coinList 	= [];
	var coinBuy		= [];
	var buyPriceArr	= [];
	var buyBalArr	= [];
	var orderArr	= [];
	var resetTF		= true;
	var startgo		= false;
	var allStop		= true;
	
	var allOrder	= [];
	$(document).ready(function(){
		initEvent();
		initMoney();
		/* for(var i=0; i<N; i++){
			buyPriceArr.push(0);
			buyBalArr.push(0);
		} */
		setTimeout(function(e){
			list()
		}, 1000)
		setTimeout(function(e){
			startCoin()
		}, 10000)
	})
	
	function initEvent(){
		$("#start").on('click', function(){
			allStop = true;
			startCoin();
		})
		
		$("#money").on('click', function(){
			money();
		})
		
		$("#stop").on('click', function(){
			allStop = false;
			console.log(allOrder)
		})
	}
	
	function token(){
		$.ajax({
			method	: 'POST',
			url		: '/test/coin/getToken',
			dataType: 'json',
			success	: function(data){
				console.log(data[0].token);
			},
			error	: function(response, ajaxOptions){
				console.log(response);
				console.log(ajaxOptions);
			}
		})
	};
	
	var buyPriceFix = 0;
	var initArr = 0;
	function initMoney(){
		coinBuy = [];
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/getMoney',
			dataType: 'json',
			async	: false,
			success	: function(data){
				$("#tbody").html('');
				var html = '';
				$.each(data, function(i,v){
					html += '<tr>';
					html += '<td id="name_'+v.currency+'">'+v.currency+'</td>';
					html += '<td id="bal_'+v.currency+'">'+v.balance+'</td>';
					html += '<td id="price_'+v.currency+'">'+v.avg_buy_price+'</td>';
					html += '</tr>';
					if(v.currency != 'KRW' && v.currency != 'APENFT'){
						coinBuy.push("KRW-"+v.currency);
						buyPriceArr[initArr] = v.avg_buy_price;
						buyBalArr[initArr] = v.balance;
						initArr++
					}
					if(v.currency == 'KRW'){
						buyPriceFix = Math.floor(parseInt((v.balance * 0.98) / N) / 100)*100;
						console.log(buyPriceFix)
					}
				})
				$("#tbody").html(html);
			},
			error	: function(response, ajaxOptions){
				//console.log("MONEY FAIL!!")
			}
		})
	}
	
	function money(){
		coinBuy = [];
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/getMoney',
			dataType: 'json',
			async	: false,
			success	: function(data){
				$("#tbody").html('');
				var html = '';
				$.each(data, function(i,v){
					html += '<tr>';
					html += '<td id="name_'+v.currency+'">'+v.currency+'</td>';
					html += '<td id="bal_'+v.currency+'">'+v.balance+'</td>';
					html += '<td id="price_'+v.currency+'">'+v.avg_buy_price+'</td>';
					html += '</tr>';
					if(v.currency != 'KRW' && v.currency != 'APENFT'){
						coinBuy.push("KRW-"+v.currency);
					}
				})
				$("#tbody").html(html);
			},
			error	: function(response, ajaxOptions){
				//console.log("MONEY FAIL!!")
			}
		})
	}
	
	function list(){
		var krwList = [];
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/getList',
			dataType: 'json',
			async	: false,
			success	: function(data){
				for(var i=0; i<data.length; i++){
					if(data[i].market.indexOf('KRW') != -1){
						krwList.push(data[i].market);
					}
				}
				getCoinRank(krwList);
			},
			error	: function(response, ajaxOptions){
				//console.log("LIST FAIL!!")
			}
		})
	}
	
	function getCoinRank(nameList){
		coinList = [];
		for(var j=0; j<nameList.length; j++){
			$.ajax({
				method	: 'GET',
				url		: '/test/coin/getCoinRank',
				dataType: 'json',
				async	: false,
				data	: {
					'name'	: nameList[j]
				},
				success	: function(data){
					var average = 0;
					for(var i=0; i<data.length; i++){
						average += data[i].change_price;
					}
					if(average > 0 && data[0].candle_acc_trade_price > 1000000000 && nameList[j].indexOf("KRW-BTC") == -1 && nameList[j].indexOf("KRW-ETH") == -1){
						coinList.push(nameList[j]);
					}
					for(var i=0; i<coinBuy.length; i++){
						coinList.push(coinBuy[i]);
					}
					if(j == nameList.length-1){
						startgo = true;
					}
				},
				error	: function(response, ajaxOptions){
					//console.log("COINRANK FAIL!!")
					if(j == nameList.length-1){
						startgo = true;
					}
				}
			})
		}
	}
	
	var coinIndex = 0;
	var cntIndex = 0;
	function startCoin(){
		console.log(coinList.length)
		setInterval(function(){
			var now = new Date();
			if(now.getHours() == 9 && resetTF){
				list();
				coinIndex = 0;
				resetTF = false;
			}else if(now.getHours() == 10 && !resetTF){
				resetTF = true;
			}
			
			if(coinIndex > coinList.length - 2){
				if(orderArr.length > 0){
					orderJosa();
				}
				coinIndex = 0;
				cntIndex++;
			}else{
				coinIndex++;
			}
			if(cntIndex != 0 && cntIndex%10 == 0){
				//money();
			}
			getCoinPrice(coinList[coinIndex]);
		}, 100);
	}
	
	function getCoinPrice(name){
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/getCoinPrice',
			dataType: 'json',
			data	: {
				'name' : name
			},
			success	: function(data){
				//console.log("COINPRICE SUCCESS!! : "+name)
				analyze(data, name);
			},
			error	: function(response, ajaxOptions){
				//console.log("COINPRICE FAIL!!")
			}
		})
	}
	
	function analyze(data, name){
		if(coinBuy.indexOf(name) != -1){
			coinSellFun(data, name);
		}else{
			coinBuyFun(data, name);
		}
	}
	
	function coinSellFun(data, name){
		var price = buyPriceArr[coinBuy.indexOf(name)];
		var bal = buyBalArr[coinBuy.indexOf(name)];
		var params = {
				'name' : name,
				'price': price,
				'bal'  : bal
		}
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/sellCoin',
			dataType: 'json',
			data	: params,
			success	: function(data){
				console.log(name + '///' + JSON.stringify(data))
				if(data[0].SELL == 'YES'){
					//orderArr.push(data.uuid);
					console.log("sell☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
					allOrder.push({
						"주문" : "ask",
						"가격" : data[0].per
					})
					buyPriceArr.splice(coinBuy.indexOf(name),1);
					buyBalArr.splice(coinBuy.indexOf(name),1);
					coinBuy.splice(coinBuy.indexOf(name),1);
					console.log(coinBuy);
					console.log(buyPriceArr);
					console.log(buyBalArr);
				}
			},
			error	: function(response, ajaxOptions){
				//console.log("COINSELL FAIL!!")
				//console.log(response);
				//console.log(ajaxOptions);
			}
		})
	}
	
	function coinBuyFun(data, name){
		if(coinBuy.length >= N){
			return false;
		}else{
		
			var tick = [];
			var sum = 0;
			for(var i=0; i<data.length; i++){
				var dif = data[i].trade_price - data[i].opening_price;
				sum += dif;
				dif > 0 ? tick.unshift(1) : tick.unshift(0);
			}
			var buy0 = buyTF(tick);
			var buy1 = buyTF2(data);
			var buyOK = '0';
			if(buy0 == '1' || buy1 == '1'){
				buyOK = '1';
			}
			if(sum > 0 && buyOK == '1'){
				var krwPrice = $("#bal_KRW").text();
				var buyPrice = buyPriceFix
				var params = {
						'name'	: name,
						'price'	: buyPrice
				}
				$.ajax({
					method	: 'GET',
					url		: '/test/coin/coinBuyGood',
					dataType: 'json',
					data	: params,
					success	: function(data){
						//console.log(JSON.stringify(data))
						if(data.uuid != null && data.uuid != '' && typeof data.uuid != 'undefined'){
							orderArr.push(data.uuid);
							coinBuy.push(data.market);
							buyPriceArr.push(0);
							buyBalArr.push(0);
							console.log("order□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□");
							
						}
					},
					error	: function(response, ajaxOptions){
						console.log("COINBUY FAIL!!")
					}
				})
			}
		}
	}
	
	function buyTF2(data){
		var D4 = data[4].trade_price - data[4].opening_price;
		var D3 = data[3].trade_price - data[3].opening_price;
		var D2 = data[2].trade_price - data[2].opening_price;
		var D1 = data[1].trade_price - data[1].opening_price;
		var D0 = data[0].trade_price - data[0].opening_price;
		
		var SF2 = data[2].opening_price - data[2].trade_price;
		var SF1 = data[1].opening_price - data[1].trade_price;
		
		var S1 = data[1].opening_price;
		var S0 = data[0].opening_price;
		
		var E1 = data[1].trade_price;
		var E0 = data[0].trade_price;
		
		var HL1 = (data[1].high_price + data[1].low_price)*0.7
		var HL0 = (data[0].high_price + data[0].low_price)*0.7
		var turn = '0';
		
		if(D2 < 0 && D1 < 0 && HL0 < S0 && HL0 < E0){
			turn = '1';
		}else if(D3 < 0 && D2 < 0 && HL1 < S1 && HL1 < E1){
			turn = '1';
		}else if(D2 < 0 && D1 < 0 && (2 * SF1 < D0)){
			turn = '1';
		}else if(D3 < 0 && D2 < 0 && (2 * SF2 < D1)){
			turn = '1';
		}
		
		if(turn == '1'){
			return '1';
		}else if(turn == '0'){
			return '2';
		}
	}
	
	function buyTF(tick){
		var score = 0;
		for(var i=0; i<tick.length-1; i++){
			var con = '';
			con = '' + tick[i] + '' + tick[i+1];
			if(con == '00'){
				score += -2;
			}else if(con == '01'){
				score += 1;
			}else if(con == '10'){
				score += -1;
			}else if(con == '11'){
				score += 2;
			}
		}
		if(score > 2){
			return "1";
		}else{
			return "2";
		}
	}
	
	function orderCancle(uuid){
		$.ajax({
			method	: 'GET',
			url		: '/test/coin/orderCancle',
			dataType: 'json',
			async	: false,
			data	: {
				'uuid'	: uuid,
			},
			success	: function(data){
				console.log("ORDERCANCEL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
			},
			error	: function(response, ajaxOptions){
				console.log("ORDERCANCEL FAIL!!")
			}
		})
	}
	
	function orderJosa(){
		var len = orderArr.length;
		for(var i=len-1; i>=0; i--){
			var params = {
					'uuid'	: orderArr[i]
			}
			$.ajax({
				method	: 'GET',
				url		: '/test/coin/orderJosa',
				dataType: 'json',
				data	: params,
				success	: function(data){
					//console.log(JSON.stringify(data))
					if(data.state != "wait"){
						if(data.side == "bid"){
							//console.log(JSON.stringify(data))
							buyPriceArr[coinBuy.indexOf(data.market)] = data.trades[0].price;
							buyBalArr[coinBuy.indexOf(data.market)] = data.executed_volume;
							allOrder.push({
								"주문" : data.side,
								"가격" : data.trades[0].price
							})
						}
						/* else if(data.trades.side == "ask"){
							buyPriceArr[coinBuy.indexOf(data.market)] = 0;
							buyBalArr[coinBuy.indexOf(data.market)] = 0;
							coinBuy.splice(coinBuy.indexOf(data.market));
						} */
						orderArr.splice(orderArr.indexOf(orderArr[i]),1)
					}
				},
				error	: function(response, ajaxOptions){
					//console.log("ORDERJOSA FAIL!!")
				}
			})
		}
	}
</script>
</head>
<body>
	<div style="margin-bottom: 20px">
		<button id="money">자산</button>
		<button id="start">시작</button>
		<button id="stop">정지</button>
	</div>
	<h1>보유자산</h1>
	<table id="table" border="1" style="text-align: center;width: 70%;margin-top: 10px">
		<colgroup>
			<col width="25%">
			<col width="25%">
			<col width="25%">
			<col width="25%">
		</colgroup>
		<tr>
			<th>이름</th>
			<th>갯수</th>
			<th>구매가</th>
			<th>판매가</th>
		</tr>
		<tbody id="tbody">
		</tbody>
	</table>
	<table id="table1" border="1" style="text-align: center;width: 70%;margin-top: 10px">
		<colgroup>
			<col width="20%">
			<col width="40%">
			<col width="40%">
		</colgroup>
		<tr>
			<th>이름</th>
			<th>구분</th>
			<th>가격</th>
		</tr>
		<tbody id="tbody1">
		</tbody>
	</table>
</body>
</html>