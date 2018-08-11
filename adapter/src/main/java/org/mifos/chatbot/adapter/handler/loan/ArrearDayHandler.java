package org.mifos.chatbot.adapter.handler.loan;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mifos.chatbot.adapter.handler.HandlerUtils;
import org.mifos.chatbot.client.ApiException;
import org.mifos.chatbot.client.api.LoansApi;
import org.mifos.chatbot.client.model.GetLoansLoanIdResponse;
import org.mifos.chatbot.core.model.Intent;
import org.mifos.chatbot.core.model.MifosResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ArrearDayHandler extends BaseLoanIntentHandler {
    private static final String[] INTENT_KEYWORDS = {"arrear", "day"};

    @Autowired
    private LoansApi loansApi;

    @Override
    public Boolean canHandle(Intent intent) {
        for(String intent_keyword : INTENT_KEYWORDS) {
            if (!intent.getKeyword().toLowerCase().contains(intent_keyword.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public MifosResponse handle(Intent intent) {
        MifosResponse response = new MifosResponse();
        try {
            GetLoansLoanIdResponse result = loansApi.retrieveLoan(intent.getParameterAsLong("ID"), false);
            List<Long> overdueSinceDate = result.getSummary().getOverdueSinceDate();
//
//            StringBuffer sb = new StringBuffer();
//            sb.append(String.format("%04d", overdueSinceDate.get(0)));
//            sb.append(String.format("%02d", overdueSinceDate.get(1)));
//            sb.append(String.format("%02d", overdueSinceDate.get(2)));
//            String overdueDateStr = sb.toString();
//            FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
//            Date overdueDate = fdf.parse(overdueDateStr);

            Date overdueDate = HandlerUtils.convertListToDate(overdueSinceDate);

            Date currDate = new Date();

            long diff = currDate.getTime() - overdueDate.getTime();

            long diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            response.setContent(String.valueOf(diffDays) + " days");
        } catch (ApiException e) {
            log.info("Error", e);
            response.setContent(e.getMessage());
        }

        return response;
    }
}
