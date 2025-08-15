package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemSubscriptionPlanBinding;
import com.retry.vuga.model.SubscriptionModels;
import java.util.List;

public class SubscriptionPlansAdapter extends RecyclerView.Adapter<SubscriptionPlansAdapter.PlanViewHolder> {
    
    private List<SubscriptionModels.SubscriptionPricingModel> plans;
    private OnPlanClickListener onPlanClickListener;
    
    public interface OnPlanClickListener {
        void onPlanClicked(SubscriptionModels.SubscriptionPricingModel plan);
    }
    
    public SubscriptionPlansAdapter(List<SubscriptionModels.SubscriptionPricingModel> plans, OnPlanClickListener listener) {
        this.plans = plans;
        this.onPlanClickListener = listener;
    }
    
    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription_plan, parent, false);
        return new PlanViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        holder.bind(plans.get(position));
    }
    
    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }
    
    public void updatePlans(List<SubscriptionModels.SubscriptionPricingModel> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
    }
    
    class PlanViewHolder extends RecyclerView.ViewHolder {
        private ItemSubscriptionPlanBinding binding;
        
        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
        
        public void bind(SubscriptionModels.SubscriptionPricingModel plan) {
            if (binding == null) return;
            
            // Set basic plan info
            binding.tvPlanName.setText(plan.getDisplayName());
            binding.tvPlanPrice.setText(plan.getFormattedPrice());
            binding.tvPlanInterval.setText(plan.getIntervalText());
            
            // Set description if available
            if (plan.getDescription() != null && !plan.getDescription().isEmpty()) {
                binding.tvPlanDescription.setText(plan.getDescription());
                binding.tvPlanDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlanDescription.setVisibility(View.GONE);
            }
            
            // Show distributor info if it's a distributor plan
            if (plan.getDistributorName() != null && !plan.getDistributorName().isEmpty()) {
                binding.tvDistributorName.setText(plan.getDistributorName());
                binding.tvDistributorName.setVisibility(View.VISIBLE);
                
                // Load distributor logo if available
                if (plan.getDistributorLogo() != null && !plan.getDistributorLogo().isEmpty()) {
                    binding.ivDistributorLogo.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                        .load(plan.getDistributorLogo())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivDistributorLogo);
                } else {
                    binding.ivDistributorLogo.setVisibility(View.GONE);
                }
            } else {
                binding.tvDistributorName.setVisibility(View.GONE);
                binding.ivDistributorLogo.setVisibility(View.GONE);
            }
            
            // Set plan type indicator
            String planType = plan.getPricingType();
            if (planType != null) {
                switch (planType.toLowerCase()) {
                    case "base":
                        binding.tvPlanType.setText("Base Plan");
                        binding.tvPlanType.setBackgroundResource(R.drawable.bg_round_rect_5);
                        break;
                    case "distributor":
                        binding.tvPlanType.setText("Distributor");
                        binding.tvPlanType.setBackgroundResource(R.drawable.bg_round_rect_10);
                        break;
                    default:
                        binding.tvPlanType.setText(planType);
                        binding.tvPlanType.setBackgroundResource(R.drawable.bg_round_rect_5);
                        break;
                }
                binding.tvPlanType.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlanType.setVisibility(View.GONE);
            }
            
            // Handle special billing periods
            switch (plan.getBillingPeriod() != null ? plan.getBillingPeriod().toLowerCase() : "") {
                case "lifetime":
                    binding.cardPlan.setBackgroundResource(R.drawable.bg_for_pro);
                    binding.tvSpecialOffer.setText("LIFETIME");
                    binding.tvSpecialOffer.setVisibility(View.VISIBLE);
                    break;
                case "yearly":
                    binding.tvSpecialOffer.setText("BEST VALUE");
                    binding.tvSpecialOffer.setVisibility(View.VISIBLE);
                    break;
                default:
                    binding.tvSpecialOffer.setVisibility(View.GONE);
                    binding.cardPlan.setBackgroundResource(R.drawable.bg_round_rect_15);
                    break;
            }
            
            // Set click listener
            binding.getRoot().setOnClickListener(v -> {
                if (onPlanClickListener != null) {
                    onPlanClickListener.onPlanClicked(plan);
                }
            });
        }
    }
}