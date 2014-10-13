/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdjustmentService {
    public static final String PHYSICAL_COUNT = "Physical Count";
    public static final String RECEIVED_FROM_ANOTHER_FACILITY = "Received from another facility";
    public static final String SENT_TO_ANOTHER_FACILITY = "Sent to another facility";
    public static final String SELECT_REASON = "--Select reason--";
    @Inject
    DbUtil dbutil;
    @Inject
    CommoditySnapshotService commoditySnapshotService;

    @Inject
    AlertsService alertsService;

    @Inject
    StockService stockService;

    public static ArrayList<AdjustmentReason> getAdjustmentReasons() {
        return new ArrayList<>(Arrays.asList(
                new AdjustmentReason(SELECT_REASON, false, false),
                new AdjustmentReason(PHYSICAL_COUNT, true, true),
                new AdjustmentReason(RECEIVED_FROM_ANOTHER_FACILITY, true, false),
                new AdjustmentReason(SENT_TO_ANOTHER_FACILITY, false, true)));
    }

    public void save(final List<Adjustment> adjustments) {
        dbutil.withDao(Adjustment.class, new DbUtil.Operation<Adjustment, String>() {
            @Override
            public String operate(Dao dao) throws SQLException {
                for (Adjustment adjustment : adjustments) {
                    dao.create(adjustment);
                    commoditySnapshotService.add(adjustment);
                    if (adjustment.isPositive()) {
                        stockService.increaseStockLevelFor(adjustment.getCommodity(), adjustment.getQuantity());
                    } else {
                        stockService.reduceStockLevelFor(adjustment.getCommodity(), adjustment.getQuantity());
                    }
                }
                return null;
            }
        });
        alertsService.disableAllMonthlyStockCountAlerts();
    }
}
