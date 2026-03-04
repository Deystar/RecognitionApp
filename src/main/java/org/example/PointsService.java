package org.example;

import java.sql.SQLException;
import org.example.config.AppPointsProperties;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

    private final AwardGivenRepository  awardGivenRepo;
    private final AwardTypeRepository   awardTypeRepo;
    private final PurchaseRepository    purchaseRepo;
    private final StoreItemRepository   storeItemRepo;
    private final TeamRepository        teamRepo;
    private final TeamAwardRepository   teamAwardRepo;
    private final AppPointsProperties   pointsProps;

    public PointsService(AwardGivenRepository awardGivenRepo,
                         AwardTypeRepository awardTypeRepo,
                         PurchaseRepository purchaseRepo,
                         StoreItemRepository storeItemRepo,
                         TeamRepository teamRepo,
                         TeamAwardRepository teamAwardRepo,
                         AppPointsProperties pointsProps) {
        this.awardGivenRepo = awardGivenRepo;
        this.awardTypeRepo  = awardTypeRepo;
        this.purchaseRepo   = purchaseRepo;
        this.storeItemRepo  = storeItemRepo;
        this.teamRepo       = teamRepo;
        this.teamAwardRepo  = teamAwardRepo;
        this.pointsProps    = pointsProps;
    }

    // -------------------------------------------------------------------------
    // Peer giving (person → person)
    // -------------------------------------------------------------------------

    public int pointsGivenThisQuarter(int userId) throws SQLException {
        return awardGivenRepo.pointsGivenThisQuarter(userId);
    }

    public int givingBalanceThisQuarter(int userId) throws SQLException {
        return pointsProps.getPeerGivingAllowance() - pointsGivenThisQuarter(userId);
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

    // -------------------------------------------------------------------------
    // Team giving (person → team)
    // -------------------------------------------------------------------------

    public int teamPointsGivenThisQuarter(int userId) throws SQLException {
        return teamAwardRepo.teamPointsGivenThisQuarter(userId);
    }

    public int teamGivingBalanceThisQuarter(int userId) throws SQLException {
        return pointsProps.getTeamGivingAllowance() - teamPointsGivenThisQuarter(userId);
    }

    public TeamAward awardTeam(int giverId, int teamId, int points, String message)
            throws SQLException {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be greater than zero.");
        }
        if (!pointsProps.isAllowSelfTeamAward() && teamRepo.isMember(teamId, giverId)) {
            throw new IllegalArgumentException("Cannot award your own team.");
        }
        int available = teamGivingBalanceThisQuarter(giverId);
        if (points > available) {
            throw new IllegalArgumentException(
                "Insufficient team giving balance. Requested: " + points + ", available: " + available);
        }
        int id = teamAwardRepo.insert(giverId, teamId, points, message);
        return new TeamAward(id, giverId, teamId, points, message, null);
    }

    // -------------------------------------------------------------------------
    // Store purchases
    // -------------------------------------------------------------------------

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
