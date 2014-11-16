package com.brew.brewshop.fragments;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.brew.brewshop.R;
import com.brew.brewshop.storage.BrewStorage;
import com.brew.brewshop.storage.Nameable;
import com.brew.brewshop.storage.NameableAdapter;
import com.brew.brewshop.storage.hops.HopsInfo;
import com.brew.brewshop.storage.hops.HopsInfoList;
import com.brew.brewshop.storage.hops.HopsStorage;
import com.brew.brewshop.storage.inventory.InventoryItem;
import com.brew.brewshop.storage.recipes.Hop;
import com.brew.brewshop.storage.recipes.HopAddition;
import com.brew.brewshop.storage.recipes.HopUsage;
import com.brew.brewshop.storage.recipes.Malt;
import com.brew.brewshop.storage.recipes.Recipe;
import com.brew.brewshop.storage.recipes.Weight;
import com.brew.brewshop.util.Util;

import java.util.Arrays;
import java.util.List;

public class HopsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    @SuppressWarnings("unused")
    private static final String TAG = HopsFragment.class.getName();
    private static final String RECIPE = "Recipe";
    private static final String INVENTORY_ITEM = "InventoryItem";
    private static final String HOP_INDEX = "HopIndex";

    private Recipe mRecipe;
    private InventoryItem mInventoryItem;
    private HopsInfoList mHopInfo;
    private BrewStorage mStorage;
    private int mHopIndex;
    NameableAdapter<HopsInfo> mAdapter;

    private Spinner mHopTypeSpinner;
    private Spinner mHopUsageSpinner;
    private TextView mDescription;
    private EditText mWeightEdit;
    private EditText mAlphaEdit;
    private EditText mTimeEdit;
    private EditText mCustomName;
    private EditText mDryHopDaysEdit;
    private View mCustomNameView;
    private View mDescriptionView;
    private View mBoilTimeView;
    private View mDryHopView;
    private View mAlphaAcidView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View root = inflater.inflate(R.layout.fragment_edit_hops, container, false);
        mHopTypeSpinner = (Spinner) root.findViewById(R.id.hops_type);
        mWeightEdit = (EditText) root.findViewById(R.id.hops_weight);
        mAlphaEdit = (EditText) root.findViewById(R.id.hops_alpha);
        mHopUsageSpinner = (Spinner) root.findViewById(R.id.hops_usage);
        mTimeEdit = (EditText) root.findViewById(R.id.boil_duration);
        mDryHopDaysEdit = (EditText) root.findViewById(R.id.dryhop_days);
        mDescription = (TextView) root.findViewById(R.id.description);
        mCustomName = (EditText) root.findViewById(R.id.custom_name);
        mCustomNameView = root.findViewById(R.id.custom_malt_layout);
        mDescriptionView = root.findViewById(R.id.description_layout);
        mBoilTimeView = root.findViewById(R.id.boil_time_view);
        mDryHopView = root.findViewById(R.id.dry_hop_view);
        mAlphaAcidView = root.findViewById(R.id.alpha_acid_view);

        setHasOptionsMenu(true);
        mStorage = new BrewStorage(getActivity());
        mHopInfo = new HopsStorage(getActivity()).getHops();

        if (state != null) {
            mRecipe = state.getParcelable(RECIPE);
            mHopIndex = state.getInt(HOP_INDEX, -1);
            mInventoryItem = state.getParcelable(INVENTORY_ITEM);
        }

        String customName = getActivity().getResources().getString(R.string.custom_hop);
        mAdapter = new NameableAdapter<HopsInfo>(getActivity(), mHopInfo, customName);
        mHopTypeSpinner.setAdapter(mAdapter);
        mHopTypeSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.hops_usage, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mHopUsageSpinner.setAdapter(adapter);
        mHopUsageSpinner.setOnItemSelectedListener(this);

        if (mInventoryItem != null) {
            root.findViewById(R.id.hops_usage_view).setVisibility(View.GONE);
            TextView title = (TextView) root.findViewById(R.id.hop_addition_title);
            title.setText(getResources().getString(R.string.inventory_hop));
            mDryHopView.setVisibility(View.GONE);
            mBoilTimeView.setVisibility(View.GONE);
            setHop(mInventoryItem.getHop());
            setWeight(mInventoryItem.getQuantity());
        } else if (mRecipe != null && mHopIndex >= 0) {
            HopAddition addition = getHopAddition();
            setHop(addition.getHop());
            setWeight(addition.getWeight());
            setHopUsageView();
            mDryHopDaysEdit.setText(String.valueOf(getHopAddition().getDryHopDays()));
            mTimeEdit.setText(String.valueOf(getHopAddition().getBoilTime()));
        }

        ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getActivity().getResources().getString(R.string.edit_hop_addition));
        }
        root.findViewById(R.id.root_view).requestFocus();
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        super.onPause();
        if (mRecipe != null) {
            updateRecipe();
        } else if (mInventoryItem != null) {
            updateInventoryItem();
        }
        Util.hideKeyboard(getActivity());
    }

    private void updateRecipe() {
        HopAddition addition = getHopAddition();
        addition.setHop(getHopData());
        addition.setWeight(getWeightData());
        CharSequence usage = (CharSequence) mHopUsageSpinner.getSelectedItem();
        addition.setUsage(HopUsage.fromString(usage.toString()));
        addition.setBoilTime(Util.toInt(mTimeEdit.getText()));
        addition.setDryHopDays(Util.toInt(mDryHopDaysEdit.getText()));
        mStorage.updateRecipe(mRecipe);
    }

    private void updateInventoryItem() {
        mInventoryItem.setIngredient(getHopData());
        mInventoryItem.setQuantity(getWeightData());
        mStorage.updateInventoryItem(mInventoryItem);
    }

    private Hop getHopData() {
        Nameable selectedHop = (Nameable) mHopTypeSpinner.getSelectedItem();
        Hop storedHop = new Hop();
        double alpha = Util.toDouble(mAlphaEdit.getText());
        if (alpha > 100) {
            alpha = 100;
        }
        storedHop.setPercentAlpha(alpha);
        mAdapter.setNamedItem(selectedHop, storedHop, mCustomName.getText().toString());
        return storedHop;
    }

    private Weight getWeightData() {
        return new Weight(0, Util.toDouble(mWeightEdit.getText()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mStorage.close();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (state == null) {
            state = new Bundle();
        }
        state.putParcelable(RECIPE, mRecipe);
        state.putParcelable(INVENTORY_ITEM, mInventoryItem);
        state.putInt(HOP_INDEX, mHopIndex);
    }

    private HopAddition getHopAddition() {
        return  mRecipe.getHops().get(mHopIndex);
    }

    private void setHop(Hop hop) {
        HopsInfo info = mHopInfo.findByName(hop.getName());
        int index = mHopInfo.indexOf(info);
        if (index < 0 ) {
            mCustomName.setText(hop.getName());
            mCustomNameView.setVisibility(View.VISIBLE);
            mDescriptionView.setVisibility(View.GONE);
            mHopTypeSpinner.setSelection(0);
        } else {
            mHopTypeSpinner.setSelection(index + 1);
            mCustomNameView.setVisibility(View.GONE);
            mDescriptionView.setVisibility(View.VISIBLE);
        }
        mAlphaEdit.setText(Util.fromDouble(hop.getPercentAlpha(), 3));
    }

    private void setWeight(Weight weight) {
        mWeightEdit.setText(Util.fromDouble(weight.getOunces(), 3));
    }

    private void setHopUsageView() {
        HopUsage usage = getHopAddition().getUsage();
        mHopUsageSpinner.setSelection(usage.ordinal());
        mBoilTimeView.setVisibility(View.GONE);
        mDryHopView.setVisibility(View.GONE);
        mAlphaAcidView.setVisibility(View.GONE);
        switch (usage) {
            case FIRST_WORT:
                mAlphaAcidView.setVisibility(View.VISIBLE);
                break;
            case BOIL:
                mBoilTimeView.setVisibility(View.VISIBLE);
                mAlphaAcidView.setVisibility(View.VISIBLE);
                break;
            case DRY_HOP:
                mDryHopView.setVisibility(View.VISIBLE);
                break;
            case WHIRLPOOL:
            default:
                break;
        }
    }

    public void setRecipe(Recipe recipe) {
        mRecipe = recipe;
    }

    public void setInventoryItem(InventoryItem item) {
        mInventoryItem = item;
    }

    public void setHopIndex(int index) {
        mHopIndex = index;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        switch (parent.getId()) {
            case R.id.hops_type:
                onHopsTypeSelected();
                break;
            case R.id.hops_usage:
                onHopsUsageSelected();
                break;
        }
    }

    private Hop getHop() {
        Hop hop = null;
        if (mRecipe != null) {
            hop = getHopAddition().getHop();
        } else if (mInventoryItem != null) {
            hop = mInventoryItem.getHop();
        }
        return hop;
    }

    private void onHopsUsageSelected() {
        String usage = mHopUsageSpinner.getSelectedItem().toString();
        if (!usage.equals(getHopAddition().getUsage())) {
            getHopAddition().setUsage(HopUsage.fromString(usage));
            setHopUsageView();
        }
    }

    private void onHopsTypeSelected() {
        Nameable item = (Nameable) mHopTypeSpinner.getSelectedItem();
        if (handleCustomName(item)) {
            return;
        }
        HopsInfo hopsInfo = (HopsInfo) item;
        if (!hopsInfo.getName().equals(getHop().getName())) {
            mAlphaEdit.setText(Util.fromDouble(hopsInfo.getAlphaAcid(), 3));
            getHop().setName(hopsInfo.getName());
        }
        if (hopsInfo.getDescription().length() == 0) {
            mDescription.setTextColor(getActivity().getResources().getColor(R.color.text_dark_secondary));
            mDescription.setText(getActivity().getResources().getString(R.string.no_description));
        } else {
            mDescription.setTextColor(getActivity().getResources().getColor(R.color.text_dark_primary));
            mDescription.setText(Util.separateSentences(hopsInfo.getDescription()));
        }
    }

    private boolean handleCustomName(Nameable item) {
        boolean handled = false;
        String customName = getActivity().getResources().getString(R.string.custom_hop);
        if (item.getName().equals(customName)) {
            if (!mCustomName.getText().toString().equals(getHop().getName())) {
                mCustomName.setText("");
                mAlphaEdit.setText("0");
            }
            mCustomNameView.setVisibility(View.VISIBLE);
            mDescriptionView.setVisibility(View.GONE);
            handled = true;
        } else {
            mCustomNameView.setVisibility(View.GONE);
            mDescriptionView.setVisibility(View.VISIBLE);
        }
        return handled;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}