/***
 * Advanced:
 *  Argument Captors
 *  Mockito BDD
 *  Mocking Static Methods
 *  Mockito answers
 *  Mocking Final
 */

package com.mockitotutorial.happyhotel.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

//@ExtendWith(MockitoExtension.class)
class BookServiceAdvancedTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private PaymentService paymentServiceMock;

    @Mock
    private RoomService roomServiceMock;

    @Spy
    private BookingDAO bookingDAOMock;

    @Mock
    private MailSender mailSenderMock;

    @Captor
	private ArgumentCaptor<Double> doubleCaptor;

	@BeforeEach
	void setup() {
		this.paymentServiceMock = mock(PaymentService.class);
		this.roomServiceMock = mock(RoomService.class);
		this.bookingDAOMock = mock(BookingDAO.class);
		this.mailSenderMock = mock(MailSender.class);

		this.bookingService = new BookingService(paymentServiceMock, roomServiceMock,
				bookingDAOMock, mailSenderMock);

		this.doubleCaptor = ArgumentCaptor.forClass(Double.class);
	}

    // Argument Captors
	@Test
	void should_PayCorrectPrice_When_InputOK() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, true);

		// when
		bookingService.makeBooking(bookingRequest);

		// then
		verify(paymentServiceMock, times(1)).pay(eq(bookingRequest), doubleCaptor.capture());
		double capturedArgument = doubleCaptor.getValue();
		assertEquals(400.0, capturedArgument);
	}

	@Test
	void should_PayCorrectPrices_When_MultipleCalls() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, true);
		BookingRequest bookingRequest2 = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 02), 2, true);
		List<Double> expectedValues = Arrays.asList(400.0, 100.0);

		// when
		bookingService.makeBooking(bookingRequest);
		bookingService.makeBooking(bookingRequest2);

		// then
		verify(paymentServiceMock, times(2)).pay(any(), doubleCaptor.capture());
		List<Double> capturedArguments = doubleCaptor.getAllValues();

		assertEquals(expectedValues, capturedArguments);
	}

    // Mockito BDD
    /**
     * 'when...thenReturn' we replace for 'given...willReturn'
     */
    @Test
    void should_CountAvailablePlaces_When_MultipleRoomsAvailable(){
        // given
        given(this.roomServiceMock.getAvailableRooms()).willReturn(Collections.singletonList(new Room("Room 1", 2)));

        int expected = 2;

        // when
        int actual = bookingService.getAvailablePlaceCount();

        // then
        assertEquals(expected, actual);
    }

    /**
     * And chenge the 'verify(parm1, times(x))' to 'then...should(times(x))'
     */
    @Test
	void should_InvokePayment_When_Prepaid() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, true);

		// when
		bookingService.makeBooking(bookingRequest);

        // then
        then(paymentServiceMock).should(times(1)).pay(bookingRequest, 400.0);
		verifyNoMoreInteractions(paymentServiceMock);
	}

    // Strict Stubbing (defining behaviour)
    // Stubbing is a good thing in general, so don't overuse the Lenient() invocation
    @Test
	void should_InvokePayment_Stubbing() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, false);
        when(paymentServiceMock.pay(any(), anyDouble())).thenReturn("1");
        //Lenient().when(paymentServiceMock.pay(any(), anyDouble())).thenReturn("1");

		// when
		bookingService.makeBooking(bookingRequest);

        // then
        // no exception is thrown
	}

    // Mocking Static Methods
    // To use this in pom.xml should have artifactId 'mockito-inlone' instend of 'mockito-core' this change make able to test static method
    @Test
    void should_CalculateCorrectPrice(){
        try (MockedStatic<CurrencyConverter> mockedConverter = mockStatic(CurrencyConverter.class)) {
            // given
            BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020,01,05), 2, false);

            double expected = 400.0;
            mockedConverter.when(() -> CurrencyConverter.toEuro(anyDouble())).thenReturn(400.0);
            
            // when
            double actual = bookingService.calculatePriceEuro(bookingRequest);

            // then
            assertEquals(expected, actual);
        }
    }

    // using mockito answers
    /**
     * In all tests above the then return a constant value, with thenAnswer. Awnsers are a slightly more advanced concept.
     */
    @Test
    void should_CalculateCorrectPrice_answer(){
        try (MockedStatic<CurrencyConverter> mockedConverter = mockStatic(CurrencyConverter.class)) {
            // given
            BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020,01,05), 2, false);

            double expected = 400.0 * 0.8;
            mockedConverter.when(() -> CurrencyConverter.toEuro(anyDouble())).thenAnswer(inv -> (double) inv.getArgument(0) * 0.8);
            
            // when
            double actual = bookingService.calculatePriceEuro(bookingRequest);

            // then
            assertEquals(expected, actual);
        }
    }

    // Mocking Final
    // To use this in pom.xml should have artifactId 'mockito-inline' instend of 'mockito-core' this change make able to test static method
    @Test
    void should_CountAvailablePlaces_When_OnRoomAvailable() {
        // given
        when(this.roomServiceMock.getAvailableRooms()).thenReturn(Collections.singletonList(new Room("Room 1", 5)));
        int expected = 5;

        // when
        int actual = bookingService.getAvailablePlaceCount();

        // then
        assertEquals(expected, actual);
    }
}
