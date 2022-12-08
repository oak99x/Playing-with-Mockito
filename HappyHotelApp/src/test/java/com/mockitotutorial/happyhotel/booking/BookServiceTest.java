/**
 * Mockito 4 Basics
 *  Default Return Values
 *  Returning Custom Values
 *  Multiple thenReturn calls
 *  Throwing Exceptions
 *  Mocking Void methods
 *  Argument Matchers
 *  Verifying Behavior
 *  Spies
 */

package com.mockitotutorial.happyhotel.booking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;



import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
class BookServiceTest {

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

    @BeforeEach
    void setup() {
        this.paymentServiceMock = mock(PaymentService.class);
        this.roomServiceMock = mock(RoomService.class);
        this.bookingDAOMock = mock(BookingDAO.class);
        this.mailSenderMock = mock(MailSender.class);

        this.bookingService = new BookingService(paymentServiceMock, roomServiceMock, bookingDAOMock, mailSenderMock);

    }

    // this first didn't use mock
    @Test
    void should_CalculateCorrectPrice_When_CorrectInput(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020,01,01), LocalDate.of(2020, 01,05), 2, false);
        double expected = 4*2*50;

        //when
        double actual = bookingService.calculatePrice(bookingRequest);

        //then
        assertEquals(expected, actual);
    }

    // default return values
    @Test
    void should_CountAvailablePlaces(){
        // given
        int expected = 0;

        // explanation
        // getAvailablePlaceCount method use getAvailableRooms from RoomService class and return a list of type Room. Nice mocks default values: empty list by default, null object, 0/flase primitives
        System.out.println("List returned " + roomServiceMock.getAvailableRooms());
        System.out.println("Object returned " + roomServiceMock.findAvailableRoomId(null));
        System.out.println("Primitive returned " + roomServiceMock.getRoomCount());

        // when
        int actual = bookingService.getAvailablePlaceCount();
        
        // then
        assertEquals(expected, actual);
    }

    // returning custom values
    @Test
    void should_CountAvailablePlaces_When_MultipleRoomsAvailable(){
        // given
        when(this.roomServiceMock.getAvailableRooms()).thenReturn(Collections.singletonList(new Room("Room 1", 2)));

        int expected = 2;

        // when
        int actual = bookingService.getAvailablePlaceCount();

        // then
        assertEquals(expected, actual);
    }

    // multiple thenReturn calls
    @Test
	void should_CountAvailablePlaces_When_CalledMultipleTimes() {
		// given
		when(this.roomServiceMock.getAvailableRooms())
				.thenReturn(Collections.singletonList(new Room("Room 1", 5)))
				.thenReturn(Collections.emptyList());
		int expectedFirstCall = 5;
		int expectedSecondCall = 0;

		// when
		int actualFirst = bookingService.getAvailablePlaceCount();
		int actualSecond = bookingService.getAvailablePlaceCount();

		// then
		assertAll(() -> assertEquals(expectedFirstCall, actualFirst),
				() -> assertEquals(expectedSecondCall, actualSecond));
	}
    
    // if no room as requested and no room is available should return a expection, to test the makebooking that call the findAvailableRoomId we could write this when that thenThrow instend of thenReturn
    @Test
	void should_ThrowException_When_NoRoomAvailable() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, false);
		when(this.roomServiceMock.findAvailableRoomId(bookingRequest))
				.thenThrow(BusinessException.class);

		// when
		Executable executable = () -> bookingService.makeBooking(bookingRequest);
		
		// then
		assertThrows(BusinessException.class, executable);
	}

    // in this test is mocked void methods
    @Test
	void should_ThrowException_When_NoRoomAvailableV2() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, false);
        // to void methods
        doThrow(new BusinessException()).when(mailSenderMock).sendBookingConfirmation(any());

		// when
		Executable executable = () -> bookingService.makeBooking(bookingRequest);
		
		// then
		assertThrows(BusinessException.class, executable);
	}

    // To mock void methods we can start with doThrow or doNothing when()
    // doNothing is the default behavior of void methods so we didn't need this to test
    @Test
	void should_NotThrowException_When_MailNotReady() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, false);
        // to void methods
        doNothing().when(mailSenderMock).sendBookingConfirmation(any());

		// when
		Executable executable = () -> bookingService.makeBooking(bookingRequest);
		
		// then
		// no exception thrown
	}

    // To flexible assert exist “argument matchers” (anyString(), anyObject(), anyVararg(), ...)
    // the diference to the last test is when the exception will happen:
    // last was in roomServiceMock >> now in paymentServiceMock
    // and this test should pass if we give to pay method both values or just one espectifing that the other parameter is eq(value) or both with any as is bellow
    // any() insted of anyDouble() in this test didn't work 
    @Test
	void should_NotCompleteBooking_When_PriceTooHigh() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, true);
		when(this.paymentServiceMock.pay(any(), anyDouble())).thenThrow(BusinessException.class);

		// when
		Executable executable = () -> bookingService.makeBooking(bookingRequest);
        // then
        assertThrows(BusinessException.class, executable);
	}

    /**Verify behaviour */
    @Test
	void should_InvokePayment_When_Prepaid() {
		// given
		BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01),
				LocalDate.of(2020, 01, 05), 2, true);

		// when
		bookingService.makeBooking(bookingRequest);

		// then
		verify(paymentServiceMock, times(1)).pay(bookingRequest, 400.0);
		verifyNoMoreInteractions(paymentServiceMock);
        // 'times' to make sure how many time this verify will be called

	}


    /** Verifying Behaviour */
    @Test
    void should_InvokePayment_When_NotPrepaid(){
        // given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 10, 01), LocalDate.of(2020, 01, 05), 2, false);

        // when
        bookingService.makeBooking(bookingRequest);

        // then
        verify(paymentServiceMock, never()).pay(any(), anyDouble());
        verifyNoMoreInteractions(paymentServiceMock);
    }

    /** Spies 
     * mock = dummy object with no real logic
     * spy = real object with real logic that we can modify
     * 
     * mocks: when(mock.method()).thenReturn()
     * spies: doReturn().when(spy).method()
    */
    @Test
    void should_InvokePayment_When_InputOK(){
        // given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 10, 01), LocalDate.of(2020, 01, 05), 2, true);

        // when
        String bookingId = bookingService.makeBooking(bookingRequest);

        // then
        verify(bookingDAOMock).save(bookingRequest);
        System.out.println("bookingId=" + bookingId);

    }

    @Test
    void should_CancelBooking_When_InputOK(){
        // given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 10, 01), LocalDate.of(2020, 01, 05), 2, true);
        bookingRequest.setRoomId("1.3");
        String bookingId = "1";

        doReturn(bookingRequest).when(bookingDAOMock).get(bookingId);

        // when
        bookingService.cancelBooking(bookingId);

        // then
        // no exception thrown
    }

    

}
