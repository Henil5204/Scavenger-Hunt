package ca.gbc.comp3074.scavengerhunt.data

import androidx.lifecycle.LiveData

// thin pass-through over the DAOs. only point of having it is that ViewModels
// don't need to know there are TWO daos behind the scenes.
class HuntRepository(
    private val huntDao: HuntDao,
    private val userDao: UserDao
) {
    // hunts
    fun observe(sort: HuntSort): LiveData<List<Hunt>> = when (sort) {
        HuntSort.NEWEST -> huntDao.observeNewest()
        HuntSort.NAME   -> huntDao.observeByName()
        HuntSort.RATING -> huntDao.observeByRating()
    }
    fun search(q: String): LiveData<List<Hunt>> = huntDao.search(q)

    suspend fun findById(id: Long) = huntDao.findById(id)
    suspend fun count() = huntDao.count()
    suspend fun insert(h: Hunt) = huntDao.insert(h)
    suspend fun update(h: Hunt) = huntDao.update(h)
    suspend fun delete(h: Hunt) = huntDao.delete(h)
    suspend fun setCompleted(id: Long, done: Boolean) = huntDao.setCompleted(id, done)

    // users
    suspend fun findUser(username: String) = userDao.findByUsername(username)
    /** true on success, false if username's already taken */
    suspend fun registerUser(user: User): Boolean = userDao.insert(user) != -1L
}
