package org.example;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
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
    private final ShoutOutRepository    shoutOutRepo;
    private final AppConfigRepository   appConfigRepo;
    private final AppPointsProperties   pointsProps;

    public PointsService(AwardGivenRepository awardGivenRepo,
                         AwardTypeRepository awardTypeRepo,
                         PurchaseRepository purchaseRepo,
                         StoreItemRepository storeItemRepo,
                         TeamRepository teamRepo,
                         TeamAwardRepository teamAwardRepo,
                         ShoutOutRepository shoutOutRepo,
                         AppConfigRepository appConfigRepo,
                         AppPointsProperties pointsProps) {
        this.awardGivenRepo = awardGivenRepo;
        this.awardTypeRepo  = awardTypeRepo;
        this.purchaseRepo   = purchaseRepo;
        this.storeItemRepo  = storeItemRepo;
        this.teamRepo       = teamRepo;
        this.teamAwardRepo  = teamAwardRepo;
        this.shoutOutRepo   = shoutOutRepo;
        this.appConfigRepo  = appConfigRepo;
        this.pointsProps    = pointsProps;
    }

    // -------------------------------------------------------------------------
    // Peer giving (person → person)
    // -------------------------------------------------------------------------

    public int pointsGivenThisQuarter(int userId) throws SQLException {
        return awardGivenRepo.pointsGivenThisQuarter(userId);
    }

    public int givingBalanceThisQuarter(int userId) throws SQLException {
        return shoutOutGivingBalance(userId);
    }

    public int totalPointsEarned(int userId) throws SQLException {
        return shoutOutRepo.totalPointsReceived(userId);
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
    // Shout-outs (person → individual or team)
    // -------------------------------------------------------------------------

    public int shoutOutValue() throws SQLException {
        return appConfigRepo.getShoutOutValue();
    }

    public int teamShoutOutValue() throws SQLException {
        return appConfigRepo.getTeamShoutOutValue();
    }

    /** Points spent from the giver's allowance on shout-outs this quarter (legacy; kept for internal use). */
    public int shoutOutPointsGivenThisQuarter(int userId) throws SQLException {
        return shoutOutRepo.pointsGivenThisQuarter(userId);
    }

    /**
     * Returns the ISO-8601 timestamp at which the current reset period began,
     * calculated from a fixed epoch (2024-01-01) and the admin-configured interval.
     */
    private String computePeriodStart(int quantity, String unit) {
        LocalDateTime epoch = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime periodStart;
        switch (unit.toUpperCase()) {
            case "DAY": {
                long days    = ChronoUnit.DAYS.between(epoch, now);
                long periods = days / quantity;
                periodStart  = epoch.plusDays(periods * quantity);
                break;
            }
            case "WEEK": {
                long days    = ChronoUnit.DAYS.between(epoch, now);
                long stride  = (long) quantity * 7;
                long periods = days / stride;
                periodStart  = epoch.plusDays(periods * stride);
                break;
            }
            case "MONTH": {
                long months  = ChronoUnit.MONTHS.between(epoch.toLocalDate(), now.toLocalDate());
                long periods = months / quantity;
                periodStart  = epoch.plusMonths(periods * quantity);
                break;
            }
            case "QUARTER": {
                long months  = ChronoUnit.MONTHS.between(epoch.toLocalDate(), now.toLocalDate());
                long stride  = (long) quantity * 3;
                long periods = months / stride;
                periodStart  = epoch.plusMonths(periods * stride);
                break;
            }
            case "YEAR": {
                long years   = ChronoUnit.YEARS.between(epoch.toLocalDate(), now.toLocalDate());
                long periods = years / quantity;
                periodStart  = epoch.plusYears(periods * quantity);
                break;
            }
            default: {
                long days    = ChronoUnit.DAYS.between(epoch, now);
                long periods = days / 14;
                periodStart  = epoch.plusDays(periods * 14);
            }
        }
        return periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    /** How many shout-outs the user is allowed to give per reset period. */
    public int shoutOutAllowance() throws SQLException {
        return appConfigRepo.getShoutOutAllowance();
    }

    /** How many shout-outs the user has given in the current reset period. */
    public int shoutOutsGivenThisPeriod(int userId) throws SQLException {
        int    quantity    = appConfigRepo.getResetIntervalQuantity();
        String unit        = appConfigRepo.getResetIntervalUnit();
        String periodStart = computePeriodStart(quantity, unit);
        return shoutOutRepo.countGivenSince(userId, periodStart);
    }

    /** Remaining number of shout-outs the user can give this period. */
    public int shoutOutGivingBalance(int userId) throws SQLException {
        return Math.max(0, shoutOutAllowance() - shoutOutsGivenThisPeriod(userId));
    }

    /** Total points a user has received via shout-outs. */
    public int shoutOutPointsReceived(int userId) throws SQLException {
        return shoutOutRepo.totalPointsReceived(userId);
    }

    /**
     * Give a shout-out to an individual user.
     * Costs shoutOutValue points from giver's quarterly pool.
     * Recipient earns shoutOutValue points.
     */
    public ShoutOut giveShoutOut(int giverId, int recipientId, String message) throws SQLException {
        if (giverId == recipientId) {
            throw new IllegalArgumentException("You cannot give a shout-out to yourself.");
        }
        int remaining = shoutOutGivingBalance(giverId);
        if (remaining <= 0) {
            throw new IllegalArgumentException(
                "You have no shout-outs remaining this period. Your balance resets on a scheduled interval.");
        }
        int value = shoutOutValue();
        return shoutOutRepo.insert(giverId, recipientId, null, value, message, List.of(recipientId));
    }

    /**
     * Give a shout-out to a whole team.
     * Costs teamShoutOutValue points from giver's quarterly pool (flat, regardless of team size).
     * Every team member except the giver (if they are a member) earns teamShoutOutValue points.
     */
    public ShoutOut giveTeamShoutOut(int giverId, int teamId, String message) throws SQLException {
        int remaining = shoutOutGivingBalance(giverId);
        if (remaining <= 0) {
            throw new IllegalArgumentException(
                "You have no shout-outs remaining this period. Your balance resets on a scheduled interval.");
        }
        int value = teamShoutOutValue();
        List<User> members = teamRepo.getMembers(teamId);
        List<Integer> recipientIds = members.stream()
            .map(User::getId)
            .filter(id -> id != giverId)
            .collect(Collectors.toList());
        if (recipientIds.isEmpty()) {
            throw new IllegalArgumentException("This team has no eligible recipients for a shout-out.");
        }
        return shoutOutRepo.insert(giverId, null, teamId, value, message, recipientIds);
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
