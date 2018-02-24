package com.google.android.tvlauncher.appsview;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v17.leanback.widget.FacetProvider;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.tvlauncher.R;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEventParameters;
import com.google.android.tvlauncher.data.PromoChannelObserver;
import com.google.android.tvlauncher.data.TvDataManager;
import com.google.android.tvlauncher.model.Channel;
import com.google.android.tvlauncher.util.porting.Edited;
import com.google.android.tvlauncher.util.porting.Reason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class RowListAdapter extends RecyclerView.Adapter<RowListAdapter.BaseViewHolder> implements AppsManager.AppsViewChangeListener {
    private static final int ADS_ROW_POSITION = 0;
    private static final int OEM_ROW_TITLE_POSITION = 1;
    private static final int ROW_TYPE_ADS = 4;
    private static final int ROW_TYPE_APPS = 1;
    private static final int ROW_TYPE_GAMES = 2;
    private static final int ROW_TYPE_OEM = 3;
    private static final int ROW_TYPE_STORE = 5;
    static final int ROW_TYPE_TITLE = 6;
    private static final String TAG = "RowListAdapter";
    private final LaunchItemsHolder mAppLaunchItems = new LaunchItemsHolder();
    private final AppsManager mAppsManager;
    private Handler mChangeHandler = new Handler();
    private final TvDataManager mDataManager;
    private final EventLogger mEventLogger;
    private final LaunchItemsHolder mGameLaunchItems = new LaunchItemsHolder();
    private final int mKeylineOffsetOne;
    private final int mKeylineOffsetThree;
    private final int mKeylineOffsetTwo;
    private final ArrayList<LaunchItem> mOemLaunchItems = new ArrayList<>();
    private OnAppsViewActionListener mOnAppsViewActionListener;
    private AppsViewFragment.OnEditModeOrderChangeCallback mOnEditModeOrderChangeCallback;
    private final PromoChannelObserver mPromoChannelObserver = new PromoChannelObserver() {
        public void onChannelChange() {
            RowListAdapter.this.notifyItemChanged(0);
        }
    };
    private PromotionRowAdapter mPromotionRowAdapter;
    private final ArrayList<Integer> mRows = new ArrayList<>();
    private final int mStoreKeylineOffset;
    private final ArrayList<LaunchItem> mStoreLaunchItems = new ArrayList<>();

    public RowListAdapter(Context paramContext, EventLogger paramEventLogger) {
        this.mEventLogger = paramEventLogger;
        this.mDataManager = TvDataManager.getInstance(paramContext);
        this.mDataManager.registerPromoChannelObserver(this.mPromoChannelObserver);
        if (!this.mDataManager.isPromoChannelLoaded()) {
            this.mDataManager.loadPromoChannel();
        }
        Resources res = paramContext.getResources();
        this.mKeylineOffsetOne = res.getDimensionPixelOffset(R.dimen.app_view_grid_offset_position_one);
        this.mKeylineOffsetTwo = res.getDimensionPixelOffset(R.dimen.app_view_grid_offset_position_two);
        this.mKeylineOffsetThree = res.getDimensionPixelOffset(R.dimen.app_view_grid_offset_position_three);
        this.mStoreKeylineOffset = res.getDimensionPixelOffset(R.dimen.app_view_grid_store_offset);
        this.mAppsManager = AppsManager.getInstance(paramContext);
    }

    private void addLaunchItemsToViewHolder(BaseViewHolder paramBaseViewHolder, int paramInt) {
        int i = this.mRows.get(paramInt);

        List<LaunchItem> localObject = null;

        if (i == ROW_TYPE_APPS) {
            localObject = this.mAppLaunchItems.getRowData(paramInt);
        } else if (i == ROW_TYPE_OEM) {
            localObject = this.mOemLaunchItems;
        } else if (i == ROW_TYPE_GAMES) {
            localObject = this.mGameLaunchItems.getRowData(paramInt);
        }

        paramInt = getPositionRelativeToTitle(paramInt);
        if ((paramInt < 0) || (paramInt >= this.mRows.size())) {
            Log.e("RowListAdapter", "RowListAdapter: Title relative position was out of bounds : " + paramInt + ", in addLauncItemToViewHolder()");
        }

        if (localObject != null && paramBaseViewHolder instanceof AppViewHolder) {
            AppViewHolder appViewHolder = (AppViewHolder) paramBaseViewHolder;

            appViewHolder.addAllLaunchItems(localObject, i == ROW_TYPE_OEM);
        }
    }

    private int getKeylineForPosition(int paramInt) {
        if ((paramInt < 0) || (paramInt > this.mRows.size() - 1)) {
            return this.mKeylineOffsetOne;
        }
        int i = getItemCount();
        int j = this.mRows.get(paramInt);
        if ((paramInt == i - 1) && (i > 3)) {
            return this.mKeylineOffsetThree;
        }
        if ((paramInt == i - 1) && (i <= 3)) {
            return this.mKeylineOffsetTwo;
        }
        if ((paramInt == i - 2) && (j != 6)) {
            return this.mKeylineOffsetTwo;
        }
        if ((paramInt == i - 3) && (this.mRows.get(paramInt + 1) == 6)) {
            return this.mKeylineOffsetTwo;
        }
        return this.mKeylineOffsetOne;
    }

    private int getPositionRelativeToTitle(int paramInt) {
        int i = this.mRows.get(paramInt);
        if ((i == 4) || (i == 3)) {
            return -1;
        }
        i = paramInt;
        while (i >= 0) {
            if (this.mRows.get(i) == 6) {
                return paramInt - i - 1;
            }
            i -= 1;
        }
        return -1;
    }

    private void onBindChannel(AdsViewHolder paramAdsViewHolder) {
        if (this.mDataManager.isPromoChannelLoaded()) {
            Channel localChannel = this.mDataManager.getPromoChannel();
            if (localChannel != null) {
                paramAdsViewHolder.itemView.setVisibility(View.VISIBLE);
                ((PromotionRowAdapter) paramAdsViewHolder.mRecyclerView.getAdapter()).setChannelId(localChannel.getId());
                return;
            }
            paramAdsViewHolder.itemView.setVisibility(View.GONE);
            return;
        }
        this.mDataManager.loadPromoChannel();
        paramAdsViewHolder.itemView.setVisibility(View.GONE);
    }

    int getBottomKeylineForEditMode(int paramInt) {
        if (paramInt == 0) {
            return getKeylineForPosition(this.mRows.lastIndexOf((1)));
        }
        return getKeylineForPosition(this.mRows.lastIndexOf((2)));
    }

    public int getItemCount() {
        return this.mRows.size();
    }

    public int getItemViewType(int paramInt) {
        return ((Integer) this.mRows.get(paramInt)).intValue();
    }

    int getTopKeylineForEditMode(int paramInt) {
        if (paramInt == 0) {
            return getKeylineForPosition(this.mRows.indexOf((1)));
        }
        return getKeylineForPosition(this.mRows.indexOf((2)));
    }

    void initRows() {
        if (!this.mAppsManager.areItemsLoaded()) {
            return;
        }
        this.mRows.clear();
        if (this.mStoreLaunchItems.size() > 0) {
            this.mRows.add((5));
        }
        if (this.mOemLaunchItems.size() > 0) {
            this.mRows.add((6));
            this.mRows.add((3));
        }
        int i;
        if (this.mAppLaunchItems.size() > 0) {
            this.mRows.add((6));
            i = 0;
            while (i < this.mAppLaunchItems.getNumRows()) {
                this.mRows.add((1));
                i += 1;
            }
        }
        if (this.mGameLaunchItems.size() > 0) {
            this.mRows.add((6));
            i = 0;
            while (i < this.mGameLaunchItems.getNumRows()) {
                this.mRows.add((2));
                i += 1;
            }
        }
        notifyDataSetChanged();
    }

    public void onBindViewHolder(BaseViewHolder paramBaseViewHolder, int paramInt) {
        if ((paramBaseViewHolder instanceof StoreViewHolder)) {
            ((StoreViewHolder) paramBaseViewHolder).addStoreItems(this.mStoreLaunchItems);
        }

        if ((paramBaseViewHolder instanceof AppViewHolder)) {
            addLaunchItemsToViewHolder(paramBaseViewHolder, paramInt);
        }

        if ((paramBaseViewHolder instanceof TitleViewHolder)) {
            paramInt += 1; // todo why?

            if (paramInt < this.mRows.size()) {
                Resources localResources = paramBaseViewHolder.itemView.getContext().getResources();

                switch (this.mRows.get(paramInt)) {
                    case 1:
                        ((TitleViewHolder) paramBaseViewHolder).setTitle(localResources.getString(R.string.app_folder_title));
                        break;
                    case 2:
                        ((TitleViewHolder) paramBaseViewHolder).setTitle(localResources.getString(R.string.game_folder_title));
                        break;
                    case 3:
                        ((TitleViewHolder) paramBaseViewHolder).setTitle(localResources.getString(R.string.oem_row_title));
                    default:
                        ((TitleViewHolder) paramBaseViewHolder).setTitle("");
                        break;
                }
            }
        } else if ((paramBaseViewHolder instanceof AdsViewHolder)) {
            onBindChannel((AdsViewHolder) paramBaseViewHolder);
        }
        paramBaseViewHolder.set();
    }

    public BaseViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        switch (paramInt) {
            case ROW_TYPE_APPS:
            case ROW_TYPE_GAMES:
            case ROW_TYPE_OEM:
                return new AppViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.apps_view_base_row_view, paramViewGroup, false)); //2130968611
            case ROW_TYPE_ADS:
                return new AdsViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.apps_promotion_row, paramViewGroup, false)); // 2130968610
            case ROW_TYPE_STORE:
                return new StoreViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.apps_view_store_row_view, paramViewGroup, false)); //2130968613
            case ROW_TYPE_TITLE:
                return new TitleViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.title_row_view, paramViewGroup, false));//2130968738
            default:
                throw new IllegalArgumentException("Unexpected row type : " + paramInt);

        }
    }

    public void onEditModeItemOrderChange(ArrayList<LaunchItem> paramArrayList, boolean paramBoolean, Pair<Integer, Integer> paramPair) {
        if (paramArrayList != null) {
            if (paramBoolean) {
                setGameLaunchItems(paramArrayList);
                // todo moved from
            } else {
                setAppLaunchItems(paramArrayList);
            }

            initRows(); // todo moved

            int i = this.mRows.indexOf(paramBoolean ? ROW_TYPE_GAMES : ROW_TYPE_APPS);

            if ((paramPair != null) && (i != -1) && (this.mOnEditModeOrderChangeCallback != null)) {
                int j = paramPair.first;
                this.mOnEditModeOrderChangeCallback.onEditModeExited(i + j, paramPair.second);
            }
        }
    }

    @Edited(reason = Reason.HOT_MESS)
    public void onLaunchItemsAddedOrUpdated(final ArrayList<LaunchItem> paramArrayList) {
        this.mChangeHandler.post(new Runnable() {
            private boolean checkItemHolders(LaunchItemsHolder paramAnonymousLaunchItemsHolder, LaunchItem paramAnonymousLaunchItem, int rowType) {
                Pair<Integer, Integer> localPair = paramAnonymousLaunchItemsHolder.findIndex(paramAnonymousLaunchItem);

                boolean isGame = paramAnonymousLaunchItem.isGame();

                if (rowType == ROW_TYPE_GAMES) {

                    if (isGame && localPair != null) {

                    }
                    int i = paramAnonymousLaunchItemsHolder.getNumRows();
                    if (paramAnonymousLaunchItemsHolder.removeItem(paramAnonymousLaunchItem) == null) {
                        // todo removed?
                    }

                    int j = RowListAdapter.this.mRows.indexOf((rowType)) + ((Integer) localPair.first).intValue();
                    rowType = RowListAdapter.this.mRows.lastIndexOf((rowType));
                    if (paramAnonymousLaunchItemsHolder.getNumRows() >= i) {
                        RowListAdapter.this.notifyItemRangeChanged(j, rowType - j + 1);
                    }
                    RowListAdapter.this.notifyItemRangeChanged(j, rowType - j);
                    RowListAdapter.this.mRows.remove(rowType);
                    RowListAdapter.this.notifyItemRemoved(rowType);
                }

                paramAnonymousLaunchItemsHolder.set(localPair, paramAnonymousLaunchItem);

                rowType = RowListAdapter.this.mRows.indexOf((rowType));
                RowListAdapter.this.notifyItemChanged(localPair.first + rowType);

                int i = paramAnonymousLaunchItemsHolder.addItemAtIndexElseEnd(RowListAdapter.this.mAppsManager.getOrderedPosition(paramAnonymousLaunchItem), paramAnonymousLaunchItem).first;
                int j = RowListAdapter.this.mRows.indexOf((rowType));
                int k = RowListAdapter.this.mRows.lastIndexOf((rowType));
                int m = i + j;

                if (j == -1) {
                    i = j;

                    RowListAdapter.this.mRows.add(i, rowType);
                    RowListAdapter.this.notifyItemInserted(i - 1);
                    return true;
                } else {
                    RowListAdapter.this.mRows.add(1, 6);
                    RowListAdapter.this.notifyItemInserted(1);
                }

                i = 2;

                if (RowListAdapter.this.mRows.indexOf((2)) == -1) {
                    RowListAdapter.this.mRows.add((6));
                    RowListAdapter.this.notifyItemInserted(RowListAdapter.this.mRows.size() - 1);
                    i = RowListAdapter.this.mRows.size();
                } else {
                    i = RowListAdapter.this.mRows.indexOf((2)) - 1;
                    RowListAdapter.this.mRows.add(i, (6));
                    RowListAdapter.this.notifyItemInserted(i);
                    i += 1;

                    RowListAdapter.this.mRows.add((6));
                    RowListAdapter.this.notifyItemInserted(RowListAdapter.this.mRows.size() - 2);
                    i = RowListAdapter.this.mRows.size();
                }


                i = k;
                if (k - j + 1 < paramAnonymousLaunchItemsHolder.getNumRows()) {
                    RowListAdapter.this.mRows.add(k + 1, (rowType));
                    i = k + 1;
                }

                if (m != i) {
                    RowListAdapter.this.notifyItemRangeChanged(m, i - m + 1);
                } else {
                    RowListAdapter.this.notifyItemChanged(i);
                }

                return true; // hm
            }

            @Edited(reason = Reason.IF_ELSE_DECOMPILE_ERROR)
            public void run() {
                int i = 0;
                Iterator localIterator = paramArrayList.iterator();
                if (localIterator.hasNext()) {
                    LaunchItem localLaunchItem = (LaunchItem) localIterator.next();
                    if (checkItemHolders(RowListAdapter.this.mAppLaunchItems, localLaunchItem, 1) ||
                            checkItemHolders(RowListAdapter.this.mGameLaunchItems, localLaunchItem, 2)) {
                        i = 1;
                    } else {
                        i = 0; //todo?
                    }
                }

                if ((i != 0) && (paramArrayList.size() > 0)) {
                    if (paramArrayList.get(0).isGame()) {
                        RowListAdapter.this.mAppsManager.saveOrderSnapshot(RowListAdapter.this.mGameLaunchItems.getData());
                    } else {
                        RowListAdapter.this.mAppsManager.saveOrderSnapshot(RowListAdapter.this.mAppLaunchItems.getData());
                    }
                }
            }
        });
    }

    public void onLaunchItemsLoaded() {
        this.mAppLaunchItems.setData(this.mAppsManager.getAppLaunchItems());
        this.mGameLaunchItems.setData(this.mAppsManager.getGameLaunchItems());
        this.mOemLaunchItems.clear();
        this.mOemLaunchItems.addAll(this.mAppsManager.getOemLaunchItems());
        this.mStoreLaunchItems.clear();
        this.mStoreLaunchItems.add(this.mAppsManager.getAppStoreLaunchItem());
        this.mStoreLaunchItems.add(this.mAppsManager.getGameStoreLaunchItem());
        this.mStoreLaunchItems.removeAll(Collections.singleton(null));
        initRows();
        this.mEventLogger.log(new LogEventParameters("open_apps_view").putParameter("app_count", this.mAppLaunchItems.size()).putParameter("game_count", this.mGameLaunchItems.size()));
    }

    public void onLaunchItemsRemoved(final ArrayList<LaunchItem> paramArrayList) {
        this.mChangeHandler.post(new Runnable() {
            private void removeItemFromHolder(LaunchItemsHolder paramAnonymousLaunchItemsHolder, LaunchItem paramAnonymousLaunchItem, int paramAnonymousInt) {
                int j = paramAnonymousLaunchItemsHolder.getNumRows();
                @Edited(reason = Reason.VARIABLE_REUSAGE)
                Pair<Integer, Integer> pair = paramAnonymousLaunchItemsHolder.removeItem(paramAnonymousLaunchItem);

                if (pair != null) {
                    int i = RowListAdapter.this.mRows.indexOf((paramAnonymousInt)) + ((Integer) pair.first).intValue();
                    paramAnonymousInt = RowListAdapter.this.mRows.lastIndexOf((paramAnonymousInt));
                    if (j > paramAnonymousLaunchItemsHolder.getNumRows()) {
                        RowListAdapter.this.mRows.remove(paramAnonymousInt);
                        RowListAdapter.this.notifyItemRemoved(paramAnonymousInt);
                        j = RowListAdapter.this.mRows.size() - 1;
                        if (RowListAdapter.this.mRows.get(j) == 6) {
                            RowListAdapter.this.mRows.remove(j);
                            RowListAdapter.this.notifyItemRemoved(j);
                        }
                        RowListAdapter.this.notifyItemRangeChanged(i, paramAnonymousInt - i);
                        return;
                    }
                    RowListAdapter.this.notifyItemRangeChanged(i, paramAnonymousInt - i + 1);
                }
            }

            public void run() {
                for (LaunchItem localLaunchItem : paramArrayList) {
                    if (localLaunchItem.isGame()) {
                        removeItemFromHolder(RowListAdapter.this.mGameLaunchItems, localLaunchItem, 2);
                    } else {
                        removeItemFromHolder(RowListAdapter.this.mAppLaunchItems, localLaunchItem, 1);
                    }
                }
            }
        });
    }

    public void onStart() {
        this.mDataManager.registerPromoChannelObserver(this.mPromoChannelObserver);
        if (this.mPromotionRowAdapter != null) {
            this.mPromotionRowAdapter.onStart();
        }
    }

    public void onStop() {
        if (this.mPromotionRowAdapter != null) {
            this.mPromotionRowAdapter.onStop();
        }
        this.mDataManager.unregisterPromoChannelObserver(this.mPromoChannelObserver);
    }

    void setAppLaunchItems(ArrayList<LaunchItem> paramArrayList) {
        this.mAppLaunchItems.setData(paramArrayList);
    }

    void setGameLaunchItems(ArrayList<LaunchItem> paramArrayList) {
        this.mGameLaunchItems.setData(paramArrayList);
    }

    void setOemLaunchItems(ArrayList<LaunchItem> paramArrayList) {
        this.mOemLaunchItems.clear();
        this.mOemLaunchItems.addAll(paramArrayList);
    }

    void setOnAppsViewActionListener(OnAppsViewActionListener paramOnAppsViewActionListener) {
        this.mOnAppsViewActionListener = paramOnAppsViewActionListener;
    }

    void setOnEditModeOrderChangeCallback(AppsViewFragment.OnEditModeOrderChangeCallback paramOnEditModeOrderChangeCallback) {
        this.mOnEditModeOrderChangeCallback = paramOnEditModeOrderChangeCallback;
    }

    private class AdsViewHolder extends RowListAdapter.BaseViewHolder {
        private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
            public void onChanged() {
                super.onChanged();
                View localView;
                if (RowListAdapter.AdsViewHolder.this.mRecyclerView != null) {
                    localView = RowListAdapter.AdsViewHolder.this.itemView;
                    if (RowListAdapter.AdsViewHolder.this.mRecyclerView.getAdapter().getItemCount() >= 1) {
                        localView.setVisibility(View.VISIBLE); // todo haha you evil ads
                    } else {
                        localView.setVisibility(View.GONE);
                    }
                }
            }
        };
        private HorizontalGridView mRecyclerView;

        AdsViewHolder(View paramView) {
            super(paramView);
            this.mRecyclerView = ((HorizontalGridView) paramView.findViewById(R.id.apps_promotion_list));
            RowListAdapter.this.mPromotionRowAdapter = new PromotionRowAdapter(paramView.getContext());
            RowListAdapter.this.mPromotionRowAdapter.registerAdapterDataObserver(this.mDataObserver);
            this.mRecyclerView.setAdapter(RowListAdapter.this.mPromotionRowAdapter);
        }
    }

    private class AppViewHolder
            extends RowListAdapter.BaseViewHolder {
        private boolean mIsOem;
        private final ArrayList<LaunchItem> mLaunchItems = new ArrayList<>();

        AppViewHolder(View paramView) {
            super(paramView);
        }

        void addAllLaunchItems(List<LaunchItem> paramList, boolean paramBoolean) {
            this.mLaunchItems.clear();
            this.mLaunchItems.addAll(paramList);
            this.mIsOem = paramBoolean;
        }

        protected int calculateOffset() {
            int i = getAdapterPosition();
            return RowListAdapter.this.getKeylineForPosition(i);
        }

        public void set() {
            super.set();
            ((AppRowView) this.itemView).setOnAppsViewActionListener(RowListAdapter.this.mOnAppsViewActionListener);
            ((AppRowView) this.itemView).addBannerViews(this.mLaunchItems, this.mIsOem);
        }
    }

    class BaseViewHolder
            extends RecyclerView.ViewHolder
            implements FacetProvider {
        BaseViewHolder(View paramView) {
            super(paramView);
        }

        protected int calculateOffset() {
            return 0;
        }

        public Object getFacet(Class<?> paramClass) {
            if (getAdapterPosition() == -1) {
                return null;
            }
            int i = calculateOffset();
            ItemAlignmentFacet.ItemAlignmentDef itemAlignmentDef = new ItemAlignmentFacet.ItemAlignmentDef();
            itemAlignmentDef.setItemAlignmentOffset(-i);
            itemAlignmentDef.setItemAlignmentOffsetPercent(50.0F);
            ItemAlignmentFacet localItemAlignmentFacet = new ItemAlignmentFacet();
            localItemAlignmentFacet.setAlignmentDefs(new ItemAlignmentFacet.ItemAlignmentDef[]{itemAlignmentDef});
            return localItemAlignmentFacet;
        }

        public void set() {
        }
    }

    private class StoreViewHolder
            extends RowListAdapter.BaseViewHolder {
        private List<LaunchItem> mItems;

        StoreViewHolder(View paramView) {
            super(paramView);
        }

        void addStoreItems(List<LaunchItem> paramList) {
            this.mItems = paramList;
        }

        protected int calculateOffset() {
            return RowListAdapter.this.mStoreKeylineOffset;
        }

        public void set() {
            super.set();
            StoreRowButtonView localStoreRowButtonView1 = (StoreRowButtonView) this.itemView.findViewById(R.id.app_store);
            StoreRowButtonView localStoreRowButtonView2 = (StoreRowButtonView) this.itemView.findViewById(R.id.game_store);
            Iterator localIterator = this.mItems.iterator();
            while (localIterator.hasNext()) {
                LaunchItem localLaunchItem = (LaunchItem) localIterator.next();
                if (AppsManager.checkIfAppStore(localLaunchItem.getPackageName())) {
                    localStoreRowButtonView1.setStoreItem(localLaunchItem, RowListAdapter.this.mOnAppsViewActionListener);
                    localStoreRowButtonView1.setVisibility(View.VISIBLE);
                } else if (AppsManager.checkIfGameStore(localLaunchItem.getPackageName())) {
                    localStoreRowButtonView2.setStoreItem(localLaunchItem, RowListAdapter.this.mOnAppsViewActionListener);
                    localStoreRowButtonView2.setVisibility(View.VISIBLE);
                } else {
                    Log.e("RowListAdapter", "Trying to add an app to store button that is not a store.");
                }
            }
        }
    }

    private class TitleViewHolder
            extends RowListAdapter.BaseViewHolder {
        private String mTitle;

        TitleViewHolder(View paramView) {
            super(paramView);
        }

        public void set() {
            ((TextView) this.itemView).setText(this.mTitle);
        }

        public void setTitle(String paramString) {
            this.mTitle = paramString;
        }
    }
}


/* Location:              ~/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/tvlauncher/appsview/RowListAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */