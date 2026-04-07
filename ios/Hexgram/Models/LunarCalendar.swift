import Foundation

// MARK: - 农历↔公历转换
// 基于寿星万年历数据，覆盖 1900-2100 年

struct LunarCalendar {

    struct LunarDate {
        let year: Int
        let month: Int
        let day: Int
        let isLeapMonth: Bool

        var displayString: String {
            let monthNames = ["正","二","三","四","五","六","七","八","九","十","冬","腊"]
            let dayNames = [
                "初一","初二","初三","初四","初五","初六","初七","初八","初九","初十",
                "十一","十二","十三","十四","十五","十六","十七","十八","十九","二十",
                "廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"
            ]
            let mStr = (isLeapMonth ? "闰" : "") + monthNames[month - 1] + "月"
            let dStr = dayNames[day - 1]
            return "农历\(year)年\(mStr)\(dStr)"
        }
    }

    // 农历数据表 1900-2100
    // 每年用一个 Int 编码：
    // Bits 0-11: 每月大小月 (1=30天, 0=29天), bit0=正月
    // Bits 12-15: 闰月月份 (0=无闰月)
    // Bit 16: 闰月大小 (1=30天, 0=29天)
    private static let lunarInfo: [Int] = [
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, // 2050-2059
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520, // 2100
    ]

    // 农历每年正月初一对应的公历日期偏移（以1900年1月31日为基准）
    private static let baseYear = 1900
    private static let baseJD = julianDay(year: 1900, month: 1, day: 31) // 1900-1-31 = 农历1900正月初一

    // 儒略日计算
    private static func julianDay(year: Int, month: Int, day: Int) -> Int {
        let a = (14 - month) / 12
        let y = year + 4800 - a
        let m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private static func fromJulianDay(_ jd: Int) -> (year: Int, month: Int, day: Int) {
        let a = jd + 32044
        let b = (4 * a + 3) / 146097
        let c = a - (146097 * b) / 4
        let d = (4 * c + 3) / 1461
        let e = c - (1461 * d) / 4
        let m = (5 * e + 2) / 153
        let day = e - (153 * m + 2) / 5 + 1
        let month = m + 3 - 12 * (m / 10)
        let year = 100 * b + d - 4800 + m / 10
        return (year, month, day)
    }

    // 某年农历有几个月（含闰月）
    private static func monthsInYear(_ year: Int) -> Int {
        return leapMonth(year) > 0 ? 13 : 12
    }

    // 某年闰哪个月 (0=无闰月)
    static func leapMonth(_ year: Int) -> Int {
        guard year >= baseYear, year - baseYear < lunarInfo.count else { return 0 }
        return (lunarInfo[year - baseYear] >> 12) & 0xF
    }

    // 某年某月天数
    private static func daysInMonth(_ year: Int, _ month: Int, _ isLeap: Bool) -> Int {
        guard year >= baseYear, year - baseYear < lunarInfo.count else { return 29 }
        if isLeap {
            // 闰月天数
            return (lunarInfo[year - baseYear] >> 16) & 1 == 1 ? 30 : 29
        }
        return (lunarInfo[year - baseYear] >> (month - 1)) & 1 == 1 ? 30 : 29
    }

    // 某年农历总天数
    private static func daysInYear(_ year: Int) -> Int {
        guard year >= baseYear, year - baseYear < lunarInfo.count else { return 354 }
        var total = 0
        let leap = leapMonth(year)
        for m in 1...12 {
            total += daysInMonth(year, m, false)
            if m == leap {
                total += daysInMonth(year, m, true)
            }
        }
        return total
    }

    // MARK: - 农历→公历

    static func lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Bool = false) -> (year: Int, month: Int, day: Int)? {
        guard year >= baseYear, year - baseYear < lunarInfo.count else { return nil }
        guard month >= 1, month <= 12, day >= 1, day <= 30 else { return nil }

        // 如果指定闰月但该年无此闰月，返回nil
        let leap = leapMonth(year)
        if isLeapMonth && leap != month { return nil }

        // 检查天数是否合法
        let maxDay = daysInMonth(year, month, isLeapMonth)
        guard day <= maxDay else { return nil }

        // 计算从1900正月初一到目标日期的总天数
        var offset = 0

        // 累加年
        for y in baseYear..<year {
            offset += daysInYear(y)
        }

        // 累加月
        let yearLeap = leapMonth(year)
        for m in 1..<month {
            offset += daysInMonth(year, m, false)
            if m == yearLeap {
                offset += daysInMonth(year, m, true)
            }
        }

        // 如果目标是闰月，还要加上正月的天数
        if isLeapMonth {
            offset += daysInMonth(year, month, false)
        }

        // 累加日
        offset += day - 1

        let targetJD = baseJD + offset
        let result = fromJulianDay(targetJD)
        return (result.year, result.month, result.day)
    }

    // MARK: - 公历→农历

    static func solarToLunar(year: Int, month: Int, day: Int) -> LunarDate? {
        let targetJD = julianDay(year: year, month: month, day: day)
        var offset = targetJD - baseJD
        guard offset >= 0 else { return nil }

        // 找到年份
        var lunarYear = baseYear
        while lunarYear - baseYear < lunarInfo.count {
            let daysThisYear = daysInYear(lunarYear)
            if offset < daysThisYear { break }
            offset -= daysThisYear
            lunarYear += 1
        }
        guard lunarYear - baseYear < lunarInfo.count else { return nil }

        // 找到月份
        let yearLeap = leapMonth(lunarYear)
        var lunarMonth = 1
        var isLeap = false

        for m in 1...12 {
            // 正常月
            let days = daysInMonth(lunarYear, m, false)
            if offset < days {
                lunarMonth = m
                isLeap = false
                break
            }
            offset -= days

            // 闰月
            if m == yearLeap {
                let leapDays = daysInMonth(lunarYear, m, true)
                if offset < leapDays {
                    lunarMonth = m
                    isLeap = true
                    break
                }
                offset -= leapDays
            }

            if m == 12 {
                lunarMonth = 12
            }
        }

        let lunarDay = offset + 1
        return LunarDate(year: lunarYear, month: lunarMonth, day: lunarDay, isLeapMonth: isLeap)
    }

    // 获取某年某月（农历）的天数
    static func daysInLunarMonth(year: Int, month: Int, isLeapMonth: Bool = false) -> Int {
        return daysInMonth(year, month, isLeapMonth)
    }
}
