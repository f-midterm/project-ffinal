package apartment.example.backend.security;

import apartment.example.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Why: @Component ทำให้คลาสนี้เป็น Spring Bean
@Component
// Why: สืบทอดจาก OncePerRequestFilter เพื่อให้แน่ใจว่า Filter นี้จะทำงานแค่ "ครั้งเดียว" ต่อหนึ่ง Request
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // Why: นี่คือเมธอดหลักของ Filter ที่จะถูกเรียกใช้สำหรับทุก Request ที่เข้ามา
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Why: ดึงค่าของ Header ที่ชื่อ "Authorization" ออกมา ซึ่งเป็นที่ที่ client จะส่ง Token มา
        final String authHeader = request.getHeader("Authorization");

        // Why: ถ้าไม่มี Header "Authorization" หรือไม่ได้ขึ้นต้นด้วย "Bearer " แสดงว่าไม่ใช่ Request
        // ที่มีการยืนยันตัวตน ให้ปล่อยผ่านไปที่ Filter ตัวต่อไปเลย
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Why: ตัดคำว่า "Bearer " ออก เพื่อให้ได้ค่า Token จริงๆ
        final String jwt = authHeader.substring(7);
        final String username = jwtUtil.extractUsername(jwt);

        // Why: ตรวจสอบว่าเราได้ username มาจาก token และยังไม่มีการยืนยันตัวตนเกิดขึ้นใน request นี้
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Why: ถ้ายังไม่มีการยืนยันตัวตน ก็ให้โหลดข้อมูล user จากฐานข้อมูล
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // Why: ตรวจสอบว่า token ที่ได้มานั้นถูกต้องและเป็นของ user คนนี้จริงหรือไม่
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                // Why: ถ้า token ถูกต้อง ให้สร้าง object Authentication ขึ้นมา
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Why: บันทึกข้อมูลการยืนยันตัวตนนี้ไว้ใน SecurityContextHolder
                // เพื่อให้ Spring Security รู้ว่า request นี้ได้รับการยืนยันตัวตนแล้ว
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Why: ส่งต่อ request และ response ไปยัง filter ตัวต่อไปใน chain
        filterChain.doFilter(request, response);
    }
}