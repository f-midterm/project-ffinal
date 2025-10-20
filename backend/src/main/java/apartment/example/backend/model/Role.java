package apartment.example.backend.model;

/**
 * Enum สำหรับกำหนดบทบาทของผู้ใช้ในระบบ
 * ROLE_USER คือผู้ใช้ทั่วไป, ROLE_ADMIN คือผู้ดูแลระบบ, ROLE_VILLAGER คือผู้ใช้ที่ได้รับอนุมัติแล้ว
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_VILLAGER
}