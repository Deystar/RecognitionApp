package org.example.dto;

public class PointsSummary {
    private int givingAllowance;
    private int givingBalance;
    private int totalEarned;
    private int totalSpent;
    private int spendableBalance;

    public PointsSummary(int givingAllowance, int givingBalance,
                         int totalEarned, int totalSpent, int spendableBalance) {
        this.givingAllowance  = givingAllowance;
        this.givingBalance    = givingBalance;
        this.totalEarned      = totalEarned;
        this.totalSpent       = totalSpent;
        this.spendableBalance = spendableBalance;
    }

    public int getGivingAllowance()  { return givingAllowance; }
    public int getGivingBalance()    { return givingBalance; }
    public int getTotalEarned()      { return totalEarned; }
    public int getTotalSpent()       { return totalSpent; }
    public int getSpendableBalance() { return spendableBalance; }
}
