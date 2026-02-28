package org.example;

import java.sql.SQLException;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

    public static final int QUARTERLY_GIVING_ALLOWANCE = 20;

    private final AwardGivenRepository awardGivenRepo;
    private final AwardTypeRepository  awardTypeRepo;
    private final PurchaseRepository   purchaseRepo;
    private final StoreItemRepository  storeItemRepo;

    public PointsService(AwardGivenRepository awardGivenRepo,
                         AwardTypeRepository awardTypeRepo,
                         PurchaseRepository purchaseRepo,
                         StoreItemRepository storeItemRepo) {
        this.awardGivenRepo = awardGivenRepo;
        this.awardTypeRepo  = awardTypeRepo;
        this.purchaseRepo   = purchaseRepo;
        this.storeItemRepo  = storeItemRepo;
    }

    public int pointsGivenThisQuarter(int userId) throws SQLException {
        return awardGivenRepo.pointsGivenThisQuarter(userId);
    }

    public int givingBalanceThisQuarter(int userId) throws SQLException {
        return QUARTERLY_GIVING_ALLOWANCE - pointsGivenThisQuarter(userId);
    }

    public int totalPointsEarned(int userId) throws SQLException {
        return awardGivenRepo.totalPointsEarned(userId);
    }

    public int totalPointsSpent(int userId) throws SQLException {
        return purchaseRepo.totalPointsSpent(userId);
    }

    public int spendableBalance(int userId) throws SQLException {
        return totalPointsEarned(userId) - totalPointsSpent(userId);
    }

    public AwardGiven giveAward(int giverId, int recipientId, int awardTypeId, String message)
            throws SQLException {
        AwardType awardType = awardTypeRepo.findById(awardTypeId)
            .orElseThrow(() -> new IllegalArgumentException("Award type not found: " + awardTypeId));
        return giveAward(giverId, recipientId, awardTypeId, awardType.getPointsCost(), message);
    }

    public AwardGiven giveAward(int giverId, int recipientId, int awardTypeId, int points, String message)
            throws SQLException {
        if (giverId == recipientId) {
            throw new IllegalArgumentException("A user cannot give an award to themselves.");
        }
        int available = givingBalanceThisQuarter(giverId);
        if (points > available) {
            throw new IllegalArgumentException(
                "Insufficient giving balance. Requested: " + points + ", available: " + available);
        }
        int id = awardGivenRepo.insert(giverId, recipientId, awardTypeId, points, message);
        return new AwardGiven(id, giverId, recipientId, awardTypeId, points, message, null);
    }

    public Purchase purchaseItem(int userId, int storeItemId) throws SQLException {
        StoreItem item = storeItemRepo.findById(storeItemId)
            .orElseThrow(() -> new IllegalArgumentException("Store item not found: " + storeItemId));

        if (item.getQuantityAvailable() != null && item.getQuantityAvailable() <= 0) {
            throw new IllegalArgumentException("Item '" + item.getName() + "' is out of stock.");
        }

        int balance = spendableBalance(userId);
        if (item.getPointsCost() > balance) {
            throw new IllegalArgumentException(
                "Insufficient points. Item costs: " + item.getPointsCost() + ", balance: " + balance);
        }

        if (item.getQuantityAvailable() != null) {
            storeItemRepo.decrementStock(storeItemId);
        }

        int id = purchaseRepo.insert(userId, storeItemId, item.getPointsCost());
        return new Purchase(id, userId, storeItemId, item.getPointsCost(), null);
    }
}
