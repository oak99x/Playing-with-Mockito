package com.in28minutes.springboot.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.junit4.SpringRunner;
import static org.mockito.Mockito.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.in28minutes.springboot.model.Course;
import com.in28minutes.springboot.service.StudentService;

//@RunWith(SpringRunner.class)
//@WebMvcTest(value = StudentController.class, secure = false)
@WebMvcTest(value = StudentController.class)
public class StudentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean   
	private StudentService studentService;

	Course mockCourse = new Course(	"Course1", 
									"Spring", 
									"10 Steps",
									Arrays.asList("Learn Maven", "Import Project", "First Example","Second Example")
								  );

	//json para passar no post
	//caso crie um objeto da para converter para json usando  new ObjectMapper().writeValueAsString(Object);
	//exemplo 
	//VendaDto dto = VendaDto.builder().build();
	//String json = new ObjectMapper().writeValueAsString(dto);
	String exampleCourseJson = "{\"name\":\"Spring\",\"description\":\"10 Steps\",\"steps\":[\"Learn Maven\",\"Import Project\",\"First Example\",\"Second Example\"]}";

	@Test
	public void retrieveDetailsForCourse() throws Exception {

		//quando chamar o serviço.methodoTal intercepta ele e devolve o que eu passei
		when(studentService.retrieveCourse(anyString(), anyString()))
		.thenReturn(mockCourse);

		// Get course as body to /students/Student1/courses/Course1
		RequestBuilder requestBuilder = MockMvcRequestBuilders
										.get("/students/Student1/courses/Course1")
										.accept(MediaType.APPLICATION_JSON) //tipo(s) de mídia fornecido(s)
										.contentType(MediaType.APPLICATION_JSON);//o conteúdo será analisado e usado para preencher o mapa de parâmetros da solicitação

		//sem .getResponse();
		// MvcResult result =  mockMvc.perform(requestBuilder)
		// 							.andExpect(status().isOk()) //Realize uma expectativa.
		// 							.andReturn();

		//com .getResponse();
		MockHttpServletResponse result =  mockMvc.perform(requestBuilder)
												.andExpect(status().isOk()) //Realize uma expectativa.
												.andReturn()
												.getResponse();
		
		//String expected = "{id:Course1,name:Spring,description:10 Steps,steps:[Learn Maven,Import Project,First Example,Second Example]}";
		//String expected = "{id:Course1,name:Spring,description:10 Steps}";
		String expected = "{\"name\":\"Spring\",\"description\":\"10 Steps\"}";

		
		//com assertEquals precissara do replaceAll para tirar as aspas duplas
		//só  acerta se a string estiver completa - diferente do JSONAssert
		//assertEquals(expected, result.getResponse().getContentAsString().replaceAll("\"", ""));

		//result sem o .getResponse(); deve chamo aqui
		//JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
		
		//result com o .getResponse() chama direto
		JSONAssert.assertEquals(expected, result.getContentAsString(), false);
	}

	@Test
	public void createStudentCourse() throws Exception {

		//passar esse curso no post
		Course mockCourse = new Course( "1", 
										"Smallest Number", 
										"1",
										Arrays.asList("1", "2", "3", "4"));

		//studentService.addCourse para responder com mockCourse
		when(studentService.addCourse(anyString(), any(Course.class)))
		.thenReturn(mockCourse);

		// Send course as body to /students/Student1/courses
		RequestBuilder requestBuilder = MockMvcRequestBuilders
										.post("/students/Student1/courses")
										.accept(MediaType.APPLICATION_JSON)
										.contentType(MediaType.APPLICATION_JSON)
										.content(exampleCourseJson);

		//MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		//MockHttpServletResponse response = result.getResponse();
		MockHttpServletResponse response =  mockMvc.perform(requestBuilder)
													.andExpect(status().isCreated())
													.andReturn()
													.getResponse();

		verify(studentService, times(1)).addCourse(anyString(), any(Course.class));
		
		assertEquals(HttpStatus.CREATED.value(), response.getStatus());
		assertEquals("http://localhost/students/Student1/courses/1", response.getHeader(HttpHeaders.LOCATION));

	}

}
