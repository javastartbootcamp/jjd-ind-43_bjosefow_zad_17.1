package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {

        return paymentRepository.findAll()
                .stream()
                .sorted((p1, p2) -> - p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isPaymentForCurrentMonth(payment))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isPaymentForGivenMonth(payment, yearMonth))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dzni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getPaymentDate().isAfter(dateTimeProvider.zonedDateTimeNow().minusDays(days)))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        return paymentRepository.findAll()
            .stream()
            .filter(payment -> isPaymentForCurrentMonth(payment))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    private boolean isPaymentForCurrentMonth(Payment payment) {
        return payment.getPaymentDate().getMonthValue() == dateTimeProvider.zonedDateTimeNow().getMonthValue()
                && payment.getPaymentDate().getYear() == dateTimeProvider.zonedDateTimeNow().getYear();
    }

    private boolean isPaymentForGivenMonth(Payment payment, YearMonth yearMonth) {
        return payment.getPaymentDate().getMonthValue() == yearMonth.getMonthValue()
                && payment.getPaymentDate().getYear() == yearMonth.getYear();
    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isPaymentForGivenMonth(payment, yearMonth))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przeyznanaych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isPaymentForGivenMonth(payment, yearMonth))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .map(paymentItem -> paymentItem.getRegularPrice().subtract(paymentItem.getFinalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> getSumForPayment(payment).compareTo(new BigDecimal(value)) > 0)
                .collect(Collectors.toSet());
    }

    private static BigDecimal getSumForPayment(Payment payment) {
        return payment.getPaymentItems()
                .stream()
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
