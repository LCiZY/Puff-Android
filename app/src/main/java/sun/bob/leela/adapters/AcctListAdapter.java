package sun.bob.leela.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import sun.bob.leela.R;
import sun.bob.leela.db.Account;
import sun.bob.leela.db.AccountHelper;
import sun.bob.leela.utils.AppConstants;

/**
 * Created by bob.sun on 16/3/19.
 */
public class AcctListAdapter extends RecyclerView.Adapter<AcctListViewHolder> {

    private static final int VIEW_TYPE_EMPTY  = 0x23;
    private static final int VIEW_TYPE_NORMAL = 0x24;

    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<Account> data;

    public AcctListAdapter(Context context, RecyclerView recyclerView) {
        super();
        this.context = context;
        this.recyclerView = recyclerView;
        data = new ArrayList<>();
        this.setHasStableIds(true);
    }

    @Override
    public AcctListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View normal = LayoutInflater.from(context)
                .inflate(R.layout.acct_list_item, parent, false);
        AcctListViewHolder viewHolder = new AcctListViewHolder(normal);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AcctListViewHolder holder, int position) {
        holder.configureWithAccount(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId (int position) {
        return data.get(position).getId();
    }

    public void loadAccountsInCategory(Long category, String searchKeyWord){
        if (category == AppConstants.CAT_ID_ALL) {
            data = AccountHelper.getInstance(context).getRecentUsed(Short.MAX_VALUE);
        }
//        else if (category == AppConstants.CAT_ID_RECENT) {
//            data = AccountHelper.getInstance(context).getRecentUsed(10);
//        }
        else{
            data = AccountHelper.getInstance(context).getAccountsByCategory(category);
        }
        data = sortAccListBySearchMatch(data, searchKeyWord);
        this.notifyDataSetChanged();
    }

    public ArrayList<Account> sortAccListBySearchMatch( ArrayList<Account> data, String searchKeyWord){
        if(TextUtils.isEmpty(searchKeyWord)) return data;
        final Integer BASE = 100;
        final Integer FACTOR = 10;
        final HashMap<Account,Integer> accToScore = new HashMap<>(data.size());
        for (Account account: data ) {
            accToScore.put(account,0);
            String name = account.getName().toLowerCase();
            searchKeyWord = searchKeyWord.toLowerCase();
            if(name.startsWith(searchKeyWord))
                accToScore.put(account, FACTOR * BASE);
            else if(name.contains(searchKeyWord))
                accToScore.put(account, FACTOR  * BASE / 2);
            for (int i = 0; i < searchKeyWord.length(); i++){
                if (searchKeyWord.charAt(i) <= (char)255)
                    continue;
                if(name.contains(searchKeyWord.charAt(i)+"")){
                     accToScore.put(account, accToScore.get(account) + BASE);
                }
            }
        }
        Comparator<Account> comparator = new Comparator<Account>(){
            @Override
            public int compare(Account o1, Account o2) {
                return -accToScore.get(o1).compareTo(accToScore.get(o2));
            }
        };
        java.util.Collections.sort(data, comparator);
        ArrayList<Account> newList = new ArrayList<>(data.size());
        //如果名称中包含任何搜索字串的字符，则显示
        //System.out.println("排序后：");
        for (Account account: data ) {
            if(accToScore.get(account) != 0)
                newList.add(account);
          //  System.out.println(account.getName());
        }
        return newList;
    }


}
