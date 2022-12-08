import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ParquimetroTest {
    
@Test
public void Moquito() {
    // create mock
    MyClass test = mock(MyClass.class);
    // define return value for method getUniqueId()
    when(test.getUniqueId()).thenReturn(43);
    // use mock in test....
    assertEquals(test.getUniqueId(), 43);

    // Uma vez criado, o Mock registra todas as interações
    //Possível verificar se interação ocorreu
    //mockcreation
    List mockedList= mock(List.class);
    //usingmockobject
    mockedList.add("one");
    mockedList.clear();
    //verification
    verify(mockedList).add("one");
    verify(mockedList).clear();

    //Mockito verifica os valores dos argumentos usando o “equals()”
    //Para verificações mais flexíveis pode-se usar os “argument matchers” (anyString(), anyObject(), anyVararg())
    //stubbingusinganyInt() argumentmatcher
    when(mockedList.get(anyInt())).thenReturn("element");
    //verifyusingan argumentmatcher
    verify(mockedList).get(anyInt());

    // Quando se usa spy(), os métodos reais são chamados (a menos que tenham sido feitos stubs destes)
    List<String> list = new LinkedList<>();
    List<String> spy = spy(list);
    //optionally, youcan stubout some methods:
    when(spy.size()).thenReturn(100);
    //using the spy calls*real* methods
    spy.add("one");
    spy.add("two");
    //prints"one" -the first elementof a list
    System.out.println(spy.get(0));
    //size() method was stubbed-100 isprinted
    System.out.println(spy.size());
    //optionally, youcan verify
    verify(spy).add("one");
    verify(spy).add("two");
    }
}