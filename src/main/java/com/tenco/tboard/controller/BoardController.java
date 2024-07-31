package com.tenco.tboard.controller;

import java.io.IOException;
import java.util.List;

import com.tenco.tboard.model.Board;
import com.tenco.tboard.model.Comment;
import com.tenco.tboard.model.User;
import com.tenco.tboard.repository.BoardRepositoryImpl;
import com.tenco.tboard.repository.CommentRepositoryImpl;
import com.tenco.tboard.repository.interfaces.BoardRepository;
import com.tenco.tboard.repository.interfaces.CommentRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/board/*")
public class BoardController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private BoardRepository boardRepository;
    private CommentRepository commentRepository;
	
	@Override
	public void init() throws ServletException {
		boardRepository = new BoardRepositoryImpl();
		commentRepository = new CommentRepositoryImpl();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		if(session == null || session.getAttribute("principal") == null ) {
			response.sendRedirect(request.getContextPath() + "/user/signin");
			return;
		}
		
		switch (action) {
		case "/delete":
			handleDeleteBoard(request, response, session);  
			break;
		case "/update":
			showEditBoardForm(request, response, session);  
			break;
		case "/create":
			showCreateBoardForm(request, response, session);  
			break;
		case "/list":
			handleListBoards(request, response, session);
			break;
		case "/view":
			showViewBoard(request, response, session);  
			break;
		case "/deleteComment":
			handleDeleteComment(request, response, session);  
			break;
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
		
	}
	
	/**
	 * 댓글 삭제 기능( GET 방식 처리) 
	 * @param request
	 * @param response
	 * @param session
	 */
	private void handleDeleteComment(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 상세 보기 화면 이동(GET 방식 처리) 
	 * @param request
	 * @param response
	 * @param session
	 */
	private void showViewBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		try {
			int boardId =  Integer.parseInt(request.getParameter("id"));
			
			Board board = boardRepository.getBoardById(boardId);
			
			if(board == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			// 현재 로그인한 사용자의 ID 
			User user = (User)session.getAttribute("principal");
			if(user != null) {
				request.setAttribute("userID", user.getId());
			}
			
			List<Comment> commentList = commentRepository.getCommentsByBoardId(boardId);
			request.setAttribute("board", board);
			request.setAttribute("commentList", commentList);
			
			request.getRequestDispatcher("/WEB-INF/views/board/view.jsp").forward(request, response);
			
		} catch (Exception e) {
			// 잘못된 접근 입니다. back();
		}
		
	}

	/**
	 * 수정 폼 화면 이동(인증 검사 반드시 처리)
	 * @param request
	 * @param response
	 * @param session
	 */
	private void showEditBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 게시글 삭제 기능 만들기 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException 
	 */
	private void handleDeleteBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {

		int boardId = Integer.parseInt(request.getParameter("id"));
		User user = (User)session.getAttribute("principal");
		Board board = boardRepository.getBoardById(boardId);
		
		if(board == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		boardRepository.deleteBoard(boardId);
		response.sendRedirect(request.getContextPath() + "/board/list");
	}

	/**
	 * 게시글 생성 화면 이동
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void showCreateBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/views/board/create.jsp").forward(request, response);
	}

	/**
	 * 페이징 처리 하기 
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void handleListBoards(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
		int page = 1; // 기본 페이지 번호 
		int pageSize = 3; // 한 페이지당 보여질 게시글에 수  
		
		try {
			 String pageStr = request.getParameter("page");
			 if(pageStr != null ) {
				 page = Integer.parseInt(pageStr);
			 }
		} catch (Exception e) {
			page = 1; 
		}
		
		int offset = (page - 1) * pageSize; // 시작 위치 계산( offset 값 계산)
 		List<Board> boardList =  boardRepository.getAllBoards(pageSize, offset);
		
		// 전체 게시글 수 조회 
		int totalBoards = boardRepository.getTotalBoardCount();
		// 총 페이지 수 계산 -->  [1][2][3][...]
		int totalPages = (int) Math.ceil((double)totalBoards / pageSize);
		
		request.setAttribute("boardList", boardList);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("currentPage", page);
		
		// 현재 로그인한 사용자 ID 설정 
		if(session != null) {
 			 User user = (User)session.getAttribute("principal");
 			 if(user != null) {
 				 request.setAttribute("userId", user.getId());
 			 }
		}
		request.getRequestDispatcher("/WEB-INF/views/board/list.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		if(session == null || session.getAttribute("principal") == null ) {
			response.sendRedirect(request.getContextPath() + "/user/signin");
			return;
		}
		
		switch (action) {
		case "/create":
			  handleCreateBoard(request, response, session);
			break;
		case "/edit":

			break;
			
		case "/addComment":
				handleAddComment(request, response, session);
			break;
		
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}



	private void handleAddComment(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
		
		// 데이터 추출
			String content = request.getParameter("content");
			int boardId = Integer.parseInt(request.getParameter("boardId"));
			User user = (User)session.getAttribute("principal");
			
		// 데이터를 저장하는 기능 
			Comment comment = Comment.builder()
									 .boardId(boardId)
									 .userId(user.getId())
									 .content(content)
									 .build();
			
			commentRepository.addComment(comment);
		// 응답 처리 
			
			
			
			
			
			
			
			
			
			response.sendRedirect(request.getContextPath() + "/board/view?id=" + boardId);
	}

	/**
	 * 게시글 생성 처리 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException 
	 */
	private void handleCreateBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
		
		// 유효성 검사 생략 
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		User user = (User)session.getAttribute("principal");
		
		Board board = Board.builder()
				.userId(user.getId())
				.title(title)
				.content(content)
				.build();
		
		boardRepository.addBoard(board);
		
		response.sendRedirect(request.getContextPath() + "/board/list?page=1");
	}

}



