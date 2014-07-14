package io.coinswap.swap;

import com.google.bitcoin.core.*;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class AtomicSwapTrade {
    public static final Coin FEE = Coin.valueOf(10);

    // exchange fee, in satoshis per 10 microcoins
    public final Coin fee;

    public final String[] coins;
    public final Coin[] quantities;

    // buy = trading currency 0 for 1
    // sell = trading 1 for 0
    public final boolean buy;

    // if true, only fill already open orders
    // if false, open a new order if neccessary
    public boolean immediate = false;

    // coins: 0 = chain A (A->B), 1 = chain B (B->A)
    // quantities: 0 = amount traded from A->B (quantity), 1 = B->A (total)
    public AtomicSwapTrade(boolean buy, String[] coins, Coin[] quantities, Coin fee) {
        this.buy = buy;
        this.coins = checkNotNull(coins);
        checkNotNull(coins[0]);
        checkNotNull(coins[1]);
        this.quantities = checkNotNull(quantities);
        checkNotNull(quantities[0]);
        checkNotNull(quantities[1]);
        this.fee = checkNotNull(fee);
    }

    public Coin getFeeAmount(boolean a) {
        Coin[] divided = quantities[a ? 0 : 1].divideAndRemainder(1000);
        long tensOfMicrocoins = divided[0].longValue();
        // ceil the number of 10*microcoins
        if(divided[1].longValue() > 0)
            tensOfMicrocoins++;
        return fee.multiply(tensOfMicrocoins);
    }

    public Map toJson() {
        Map data = new JSONObject();
        data.put("buy", buy);
        data.put("fee", fee.longValue());
        data.put("coins", coins);
        data.put("quantities", new long[]{ quantities[0].longValue(), quantities[1].longValue() });
        data.put("immediate", immediate);
        return data;
    }

    public static AtomicSwapTrade fromJson(Map data) {
        checkNotNull(data);
        List<Integer> longQuantities = (ArrayList<Integer>) checkNotNull(data.get("quantities"));
        checkState(longQuantities.size() == 2);
        checkNotNull(longQuantities.get(0));
        checkNotNull(longQuantities.get(1));
        Coin[] quantities = new Coin[]{
                Coin.valueOf(longQuantities.get(0)),
                Coin.valueOf(longQuantities.get(1))
        };

        List<String> coins = (ArrayList<String>) checkNotNull(data.get("coins"));
        checkState(coins.size() == 2);
        checkNotNull(coins.get(0));
        checkNotNull(coins.get(1));

        AtomicSwapTrade output = new AtomicSwapTrade(
                (boolean) checkNotNull(data.get("buy")),
                new String[]{ coins.get(0), coins.get(1) },
                quantities,
                Coin.valueOf((int) checkNotNull(data.get("fee"))));
        output.immediate = (boolean) checkNotNull(data.get("immediate"));
        return output;
    }
}
